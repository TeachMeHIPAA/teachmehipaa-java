package com.teachmehipaa.generator;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * What the IR asks for, decided once: which types become models and which become request
 * parameters, union memberships and fallbacks, and how each method's arguments map onto the wire.
 * The writers only render these decisions.
 */
final class Surface {
  static final String MODEL_PACKAGE = "com.teachmehipaa.model";
  static final String PARAM_PACKAGE = "com.teachmehipaa.param";
  static final String SERVICE_PACKAGE = "com.teachmehipaa.service";

  private static final String ORGANIZATION_PREFIX = "/organizations/{orgId}/";
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");
  private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");
  private static final Set<String> BODY_TYPES =
      Set.of("application/json", "application/merge-patch+json", "multipart/form-data");

  /** Names the generated methods use themselves. */
  private static final Set<String> RESERVED_ARGUMENTS = Set.of("params", "options", "request");

  /** Where a params field goes on the wire. */
  enum Location {
    BODY,
    QUERY,
    IF_MATCH
  }

  /**
   * One setter of a params class.
   *
   * @param wireName the JSON property or query parameter name
   * @param identifier the Java field, getter and setter name
   */
  record ParamField(
      String wireName,
      String identifier,
      Ir.TypeRef type,
      boolean required,
      boolean nullable,
      String description,
      Location location) {}

  record FixedValue(List<String> path, JsonNode value) {}

  /** One resource method, with its Java name and how its arguments reach the wire. */
  record MethodPlan(
      List<String> resourcePath,
      Ir.Method method,
      String javaName,
      String where,
      String pathTemplate,
      List<String> pathArguments,
      String paramsClass,
      boolean paramsRequired,
      boolean mergePatch,
      List<ParamField> paramFields,
      List<FixedValue> fixedValues) {
    boolean hasParams() {
      return paramsClass != null;
    }

    /** The sibling that returns a binary response as a stream, or {@code null}. */
    String streamName() {
      return method.response() instanceof Ir.BinaryResponse ? javaName + "Stream" : null;
    }

    /** The sibling that sends {@code If-None-Match}, or {@code null}. */
    String ifModifiedName() {
      return "optional".equals(method.ifNoneMatch()) ? javaName + "IfModified" : null;
    }

    /** Every public method name this plan generates. */
    List<String> javaNames() {
      List<String> names = new ArrayList<>(List.of(javaName));
      if (streamName() != null) {
        names.add(streamName());
      }
      if (ifModifiedName() != null) {
        names.add(ifModifiedName());
      }
      return names;
    }
  }

  /** A node of the client's resource tree: {@code partners} holds {@code organizations}. */
  record ResourceNode(
      List<String> path, List<MethodPlan> methods, Map<String, ResourceNode> children) {
    String serviceClass() {
      return Naming.serviceClass(path);
    }

    String accessor() {
      return path.get(path.size() - 1);
    }

    boolean organizationScoped() {
      return methods.stream().allMatch(plan -> plan.method().orgScoped())
          && children.values().stream().allMatch(ResourceNode::organizationScoped);
    }
  }

  final Ir ir;
  final Map<String, Ir.IrType> types = new TreeMap<>();

  /** Every type rendered in the model package. */
  final Set<String> models = new TreeSet<>();

  /** Models a call returns directly, which carry the response they came from. */
  final Set<String> resources = new TreeSet<>();

  /** Models a call returns as an Operation, which can poll for their final state. */
  final Set<String> operations = new TreeSet<>();

  /** Object and union types rendered in the param package. */
  final Set<String> params = new TreeSet<>();

  /** For each nested param type, the fields every method that sends it fixes itself. */
  final Map<String, Set<String>> omittedParamFields = new HashMap<>();

  /** Model union to the model an unknown variant reads as. */
  final Map<String, String> fallbacks = new TreeMap<>();

  /** Fallbacks that are generated rather than taken from the IR. */
  final Set<String> unknownVariants = new TreeSet<>();

  /** Type to the unions it is a variant of, per package. */
  final Map<String, Set<String>> modelUnionsOf = new HashMap<>();

  final Map<String, Set<String>> paramUnionsOf = new HashMap<>();
  final List<MethodPlan> methods = new ArrayList<>();
  final Map<String, ResourceNode> roots = new TreeMap<>();

  Surface(Ir ir) {
    this.ir = ir;
    ir.types()
        .forEach(
            type -> {
              if (types.put(type.name(), type) != null) {
                throw new GeneratorException("type " + type.name() + " is declared twice");
              }
            });
    List<Ir.Resource> resourcesInOrder = new ArrayList<>(ir.resources());
    resourcesInOrder.sort(Comparator.comparing(resource -> String.join(".", resource.path())));
    for (Ir.Resource resource : resourcesInOrder) {
      for (Ir.Method method : resource.methods()) {
        methods.add(plan(resource.path(), method));
      }
    }
    methods.sort(Comparator.comparing(MethodPlan::where));
    collectModels();
    collectParams();
    buildTree();
  }

  Ir.IrType type(String name, String where) {
    Ir.IrType type = types.get(name);
    if (type == null) {
      throw new GeneratorException(where + ": no type " + name);
    }
    return type;
  }

  Ir.ObjectType objectType(Ir.TypeRef ref, String where) {
    if (ref instanceof Ir.Ref named && type(named.name(), where) instanceof Ir.ObjectType object) {
      return object;
    }
    throw new GeneratorException(where + ": expected a reference to an object type");
  }

  boolean isEnum(String name) {
    return types.get(name) instanceof Ir.EnumType;
  }

  /** A union's variants in IR order, without its fallback. */
  static List<String> variants(Ir.UnionType union) {
    return union.discriminator().mapping().values().stream().distinct().toList();
  }

  // --- methods ---------------------------------------------------------------------------------

  private MethodPlan plan(List<String> resourcePath, Ir.Method method) {
    String where = String.join(".", resourcePath) + "." + method.name();
    String javaName = Naming.methodName(method.name(), method.operationId(), where);
    if (!HTTP_METHODS.contains(method.httpMethod())) {
      throw new GeneratorException(where + ": no rule for HTTP method " + method.httpMethod());
    }
    if (!method.headerParams().isEmpty()) {
      throw new GeneratorException(where + ": header parameters have no rule");
    }
    if (!"none".equals(method.ifNoneMatch()) && !"optional".equals(method.ifNoneMatch())) {
      throw new GeneratorException(where + ": a required If-None-Match has no rule");
    }
    String template = method.path().template();
    if (method.orgScoped() != template.startsWith(ORGANIZATION_PREFIX)) {
      throw new GeneratorException(where + ": orgScoped disagrees with " + template);
    }
    List<String> placeholders = new ArrayList<>();
    Matcher placeholder = PLACEHOLDER.matcher(template);
    while (placeholder.find()) {
      placeholders.add(placeholder.group(1));
    }
    if (method.orgScoped()) {
      placeholders.remove(0);
    }
    List<String> declared = method.pathParams().stream().map(Ir.Param::name).toList();
    if (!placeholders.equals(declared)) {
      throw new GeneratorException(
          where + ": path params " + declared + " don't match " + template);
    }
    List<String> pathArguments = new ArrayList<>();
    for (Ir.Param param : method.pathParams()) {
      if (!param.required()
          || !(param.type() instanceof Ir.Primitive primitive)
          || !primitive.type().equals("string")) {
        throw new GeneratorException(where + ": path parameter " + param.name());
      }
      pathArguments.add(argument(param.name(), where));
    }
    String path =
        method.path().customVerb() == null ? template : template + ":" + method.path().customVerb();

    List<ParamField> fields = new ArrayList<>();
    List<FixedValue> fixed = new ArrayList<>();
    boolean mergePatch = false;
    Ir.RequestBody body = method.requestBody();
    if (body != null) {
      mergePatch = bodyFields(body, where, fields, fixed);
    }
    for (Ir.Param query : method.queryParams()) {
      if (query.type() instanceof Ir.ArrayOf || query.type() instanceof Ir.MapOf) {
        throw new GeneratorException(
            where + ": query parameter " + query.name() + " is not scalar");
      }
      fields.add(
          new ParamField(
              query.name(),
              argument(query.name(), where),
              query.type(),
              query.required(),
              false,
              query.description(),
              Location.QUERY));
    }
    if (!"none".equals(method.ifMatch())) {
      fields.add(
          new ParamField(
              "if-match",
              "ifMatch",
              new Ir.Primitive("string"),
              "required".equals(method.ifMatch()),
              false,
              null,
              Location.IF_MATCH));
    }
    Set<String> names = new TreeSet<>(pathArguments);
    for (ParamField field : fields) {
      if (!names.add(field.identifier()) || field.identifier().equals("build")) {
        throw new GeneratorException(where + ": \"" + field.identifier() + "\" is used twice");
      }
    }
    if (body != null && fields.stream().noneMatch(field -> field.location() == Location.BODY)) {
      throw new GeneratorException(where + ": a body every field of which is fixed has no rule");
    }
    if ("optional".equals(method.ifNoneMatch())
        && (!(method.response() instanceof Ir.ModelResponse) || !fields.isEmpty())) {
      throw new GeneratorException(
          where + ": If-None-Match has a rule only for a model read without parameters");
    }
    String paramsClass = fields.isEmpty() ? null : Naming.paramsClass(resourcePath, method.name());
    boolean paramsRequired =
        fields.stream().anyMatch(ParamField::required) || (body != null && body.required());
    return new MethodPlan(
        List.copyOf(resourcePath),
        method,
        javaName,
        where,
        path,
        List.copyOf(pathArguments),
        paramsClass,
        paramsRequired,
        mergePatch,
        List.copyOf(fields),
        List.copyOf(fixed));
  }

  private static String argument(String wireName, String where) {
    String name = Naming.identifier(wireName, where);
    if (!name.equals(wireName) || RESERVED_ARGUMENTS.contains(name)) {
      throw new GeneratorException(where + ": parameter \"" + wireName + "\" needs a rename rule");
    }
    return name;
  }

  /** Adds the body's settable fields; returns whether it is a merge patch. */
  private boolean bodyFields(
      Ir.RequestBody body, String where, List<ParamField> fields, List<FixedValue> fixed) {
    if (!BODY_TYPES.contains(body.contentType())) {
      throw new GeneratorException(where + ": no rule for " + body.contentType() + " bodies");
    }
    Ir.ObjectType object = objectType(body.type(), where + " body");
    boolean mergePatch = body.contentType().equals("application/merge-patch+json");
    if (mergePatch != object.mergePatch()) {
      throw new GeneratorException(where + ": mergePatch disagrees with the content type");
    }
    Map<String, Ir.TypeRef> narrowed = new HashMap<>();
    Set<String> omitted = new TreeSet<>();
    Map<String, Set<String>> nestedOmissions = new HashMap<>();
    for (Ir.Constraint constraint : body.constraints()) {
      List<String> path = constraint.path();
      Ir.Field top = field(object, path.get(0), where);
      if (constraint instanceof Ir.Variant variant && path.size() == 1) {
        narrowed.put(top.name(), new Ir.Ref(presenceMember(top.type(), variant, where)));
      } else if (constraint instanceof Ir.Const value && path.size() == 1) {
        omitted.add(top.name());
        fixed.add(new FixedValue(path, value.value()));
      } else if (constraint instanceof Ir.Const value && path.size() == 2) {
        Ir.ObjectType nested = objectType(top.type(), where + "." + top.name());
        field(nested, path.get(1), where);
        nestedOmissions.computeIfAbsent(nested.name(), name -> new TreeSet<>()).add(path.get(1));
        fixed.add(new FixedValue(path, value.value()));
      } else {
        throw new GeneratorException(where + ": no rule for constraint " + constraint);
      }
    }
    Set<String> binary =
        body.binaryParts().stream().map(Ir.BinaryPart::name).collect(Collectors.toSet());
    boolean multipart = body.contentType().equals("multipart/form-data");
    for (Ir.Field field : object.fields()) {
      boolean isBinary =
          field.type() instanceof Ir.Primitive primitive && primitive.type().equals("binary");
      if (isBinary != binary.contains(field.name()) || (isBinary && !multipart)) {
        throw new GeneratorException(where + ": binary parts disagree with " + field.name());
      }
      if (omitted.contains(field.name())) {
        continue;
      }
      Ir.TypeRef type = narrowed.getOrDefault(field.name(), field.type());
      // A type sent by several methods must leave out the same fields in every one of them.
      for (String name : refs(type)) {
        Set<String> pins = nestedOmissions.getOrDefault(name, Set.of());
        Set<String> previous = omittedParamFields.putIfAbsent(name, pins);
        if (previous != null && !previous.equals(pins)) {
          throw new GeneratorException(where + ": " + name + " is fixed differently by methods");
        }
      }
      fields.add(
          new ParamField(
              field.name(),
              Naming.identifier(field.name(), where),
              type,
              field.required(),
              field.nullable(),
              field.description(),
              Location.BODY));
    }
    return mergePatch;
  }

  private static Ir.Field field(Ir.ObjectType object, String name, String where) {
    return object.fields().stream()
        .filter(field -> field.name().equals(name))
        .findFirst()
        .orElseThrow(
            () -> new GeneratorException(where + ": " + object.name() + " has no " + name));
  }

  private String presenceMember(Ir.TypeRef ref, Ir.Variant variant, String where) {
    if (ref instanceof Ir.Ref named
        && type(named.name(), where) instanceof Ir.UnionType union
        && union.discriminator() instanceof Ir.PresenceDiscriminator presence
        && presence.mapping().containsKey(variant.requiredProperty())) {
      return presence.mapping().get(variant.requiredProperty());
    }
    throw new GeneratorException(
        where + ": no presence-union member requires " + variant.requiredProperty());
  }

  static List<String> refs(Ir.TypeRef ref) {
    if (ref instanceof Ir.Ref named) {
      return List.of(named.name());
    }
    if (ref instanceof Ir.ArrayOf array) {
      return refs(array.items());
    }
    if (ref instanceof Ir.MapOf map) {
      return refs(map.values());
    }
    return List.of();
  }

  // --- models ----------------------------------------------------------------------------------

  private void collectModels() {
    List<String> roots = new ArrayList<>();
    for (MethodPlan plan : methods) {
      Ir.Response response = plan.method().response();
      String root = null;
      if (response instanceof Ir.ModelResponse model) {
        root = named(model.type(), plan.where());
      } else if (response instanceof Ir.OperationResponse operation) {
        root = named(operation.type(), plan.where());
        operations.add(root);
      } else if (response instanceof Ir.WriteResultResponse writeResult) {
        root = named(writeResult.type(), plan.where());
        Ir.Field created =
            field(objectType(writeResult.type(), plan.where()), "created", plan.where());
        if (!created.required()
            || !(created.type() instanceof Ir.Primitive flag)
            || !flag.type().equals("boolean")) {
          throw new GeneratorException(plan.where() + ": created is not a required boolean");
        }
      } else if (response instanceof Ir.PageResponse page) {
        roots.add(pageItem(plan, page));
      } else if (response instanceof Ir.BinaryResponse || response instanceof Ir.EmptyResponse) {
        continue;
      }
      if (root != null) {
        roots.add(root);
        resources.add(root);
      }
    }
    roots.add(ir.events().envelope());
    roots.add(ir.events().unknownEvent());
    roots.add(ir.events().testDelivery().type());

    Deque<String> pending = new ArrayDeque<>(roots);
    while (!pending.isEmpty()) {
      String name = pending.pop();
      if (!models.add(name)) {
        continue;
      }
      Ir.IrType type = type(name, name);
      if (type instanceof Ir.ObjectType object) {
        object.fields().forEach(field -> pending.addAll(refs(field.type())));
      } else if (type instanceof Ir.UnionType union) {
        String fallback = fallback(union);
        fallbacks.put(name, fallback);
        List<String> members = new ArrayList<>(variants(union));
        if (types.containsKey(fallback)) {
          members.add(fallback);
        } else {
          unknownVariants.add(fallback);
          modelUnionsOf.computeIfAbsent(fallback, key -> new TreeSet<>()).add(name);
        }
        for (String member : members) {
          if (!(type(member, name) instanceof Ir.ObjectType object)) {
            throw new GeneratorException(name + ": variant " + member + " is not an object");
          }
          if (union.discriminator() instanceof Ir.PropertyDiscriminator property) {
            Ir.Field tag = field(object, property.property(), name + " variant " + member);
            if (!(tag.type() instanceof Ir.Primitive text) || !text.type().equals("string")) {
              throw new GeneratorException(member + ": discriminator is not a string");
            }
          }
          modelUnionsOf.computeIfAbsent(member, key -> new TreeSet<>()).add(name);
          pending.push(member);
        }
      }
    }
    // A union a call returns passes its response on to whichever variant it reads as.
    for (String root : List.copyOf(resources)) {
      if (types.get(root) instanceof Ir.UnionType union) {
        resources.addAll(variants(union));
        resources.add(fallbacks.get(root));
      }
    }
  }

  private String fallback(Ir.UnionType union) {
    if (union.discriminator() instanceof Ir.PresenceDiscriminator presence) {
      if (presence.fallback() == null) {
        throw new GeneratorException(union.name() + ": a response union needs a fallback");
      }
      return presence.fallback();
    }
    if (union.name().equals(ir.events().envelope())) {
      return ir.events().unknownEvent();
    }
    String unknown = "Unknown" + union.name();
    if (types.containsKey(unknown)) {
      throw new GeneratorException(unknown + " collides with an IR type");
    }
    return unknown;
  }

  private String named(Ir.TypeRef ref, String where) {
    if (ref instanceof Ir.Ref named) {
      type(named.name(), where);
      return named.name();
    }
    throw new GeneratorException(where + ": the response is not a named type");
  }

  private String pageItem(MethodPlan plan, Ir.PageResponse page) {
    Ir.Pagination pagination = plan.method().pagination();
    if (pagination == null) {
      throw new GeneratorException(plan.where() + ": a page response needs pagination");
    }
    Ir.ObjectType body = objectType(page.type(), plan.where());
    Map<String, Ir.Field> fields =
        body.fields().stream().collect(Collectors.toMap(Ir.Field::name, Function.identity()));
    Ir.Field items = fields.get("items");
    Ir.Field nextCursor = fields.get("nextCursor");
    if (fields.size() != 2
        || items == null
        || !items.required()
        || items.nullable()
        || !items.type().equals(new Ir.ArrayOf(pagination.itemType()))
        || nextCursor == null
        || !nextCursor.required()
        || !nextCursor.nullable()
        || !nextCursor.type().equals(new Ir.Primitive("string"))) {
      throw new GeneratorException(
          plan.where() + ": " + body.name() + " is not {items, nextCursor}");
    }
    if (plan.method().queryParams().stream()
        .noneMatch(p -> p.name().equals(pagination.cursorParam()))) {
      throw new GeneratorException(
          plan.where() + ": no query parameter " + pagination.cursorParam());
    }
    return named(pagination.itemType(), plan.where());
  }

  // --- params ----------------------------------------------------------------------------------

  private void collectParams() {
    Deque<String> pending = new ArrayDeque<>();
    for (MethodPlan plan : methods) {
      for (ParamField field : plan.paramFields()) {
        pending.addAll(refs(field.type()));
      }
    }
    while (!pending.isEmpty()) {
      String name = pending.pop();
      Ir.IrType type = type(name, name);
      if (type instanceof Ir.EnumType) {
        models.add(name);
        continue;
      }
      if (!params.add(name)) {
        continue;
      }
      if (models.contains(name)) {
        throw new GeneratorException(name + " is both sent and returned; it has no rule");
      }
      if (type instanceof Ir.ObjectType object) {
        object.fields().forEach(field -> pending.addAll(refs(field.type())));
      } else if (type instanceof Ir.UnionType union) {
        for (String member : variants(union)) {
          if (!(type(member, name) instanceof Ir.ObjectType object)) {
            throw new GeneratorException(name + ": variant " + member + " is not an object");
          }
          if (union.discriminator() instanceof Ir.PropertyDiscriminator property) {
            field(object, property.property(), name + " variant " + member);
            if (union.discriminator().mapping().values().stream().filter(member::equals).count()
                != 1) {
              throw new GeneratorException(name + ": " + member + " has more than one tag");
            }
          }
          paramUnionsOf.computeIfAbsent(member, key -> new TreeSet<>()).add(name);
          pending.push(member);
        }
      }
    }
    for (String name : params) {
      if (models.contains(name)) {
        throw new GeneratorException(name + " is both sent and returned; it has no rule");
      }
    }
  }

  /** The tag a request variant sends, or {@code null} when it isn't a property-union variant. */
  Map.Entry<String, String> tagOf(String member) {
    for (String unionName : paramUnionsOf.getOrDefault(member, Set.of())) {
      Ir.UnionType union = (Ir.UnionType) types.get(unionName);
      if (union.discriminator() instanceof Ir.PropertyDiscriminator property) {
        for (Map.Entry<String, String> tag : property.mapping().entrySet()) {
          if (tag.getValue().equals(member)) {
            return Map.entry(property.property(), tag.getKey());
          }
        }
      }
    }
    return null;
  }

  // --- resource tree ---------------------------------------------------------------------------

  private void buildTree() {
    Map<String, List<MethodPlan>> byPath = new LinkedHashMap<>();
    for (MethodPlan plan : methods) {
      byPath
          .computeIfAbsent(String.join(".", plan.resourcePath()), key -> new ArrayList<>())
          .add(plan);
    }
    for (MethodPlan plan : methods) {
      Map<String, ResourceNode> level = roots;
      List<String> path = plan.resourcePath();
      for (int depth = 1; depth <= path.size(); depth++) {
        List<String> prefix = path.subList(0, depth);
        ResourceNode node =
            level.computeIfAbsent(
                prefix.get(depth - 1),
                key ->
                    new ResourceNode(
                        List.copyOf(prefix),
                        byPath.getOrDefault(String.join(".", prefix), List.of()).stream()
                            .sorted(Comparator.comparing(MethodPlan::javaName))
                            .toList(),
                        new TreeMap<>()));
        level = node.children();
      }
    }
    Set<String> classes = new TreeSet<>();
    walk(
        roots.values(),
        node -> {
          Set<String> javaNames = new TreeSet<>();
          node.methods().stream()
              .flatMap(plan -> plan.javaNames().stream().map(name -> Map.entry(plan, name)))
              .forEach(
                  named -> {
                    if (!javaNames.add(named.getValue())) {
                      throw new GeneratorException(
                          named.getKey().where()
                              + ": two methods share the name "
                              + named.getValue());
                    }
                  });
          if (node.children().keySet().stream().anyMatch(javaNames::contains)) {
            throw new GeneratorException(
                node.serviceClass() + ": a method and a resource share a name");
          }
          if (!classes.add(node.serviceClass())) {
            throw new GeneratorException(node.serviceClass() + " is generated twice");
          }
        });
  }

  static void walk(Iterable<ResourceNode> nodes, java.util.function.Consumer<ResourceNode> visit) {
    for (ResourceNode node : nodes) {
      visit.accept(node);
      walk(node.children().values(), visit);
    }
  }
}

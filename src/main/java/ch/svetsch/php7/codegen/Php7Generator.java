package ch.svetsch.php7.codegen;

import io.swagger.codegen.v3.*;
import io.swagger.codegen.v3.generators.php.PhpClientCodegen;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Php7Generator extends PhpClientCodegen {

    protected String apiVersion = "0.1.0";

    /**
     * Configures the type of generator.
     *
     * @return the CodegenType for this generator
     * @see io.swagger.codegen.CodegenType
     */
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    /**
     * Configures a friendly name for the generator.  This will be used by the generator
     * to select the library with the -l flag.
     *
     * @return the friendly name for the generator
     */
    public String getName() {
        return "php7";
    }

    /**
     * Returns human-friendly help for the generator.  Provide the consumer with help
     * tips, parameters here
     *
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a php7 client library.";
    }

    public Php7Generator() {
        super();

        // set the output folder here
        outputFolder = "generated-code/php7";

        /*
         * Template Location.  This is the location which templates will be read from.  The generator
         * will use the resource stream to attempt to read the templates.
         */
        templateDir = "php7";

        /*
         * Additional Properties.  These values can be passed to the templates and
         * are available in models, apis, and supporting files
         */
        additionalProperties.put("apiVersion", apiVersion);

        this.typeMapping.put("date", "\\DateTimeImmutable");
        this.typeMapping.put("Date", "\\DateTimeImmutable");
        this.typeMapping.put("DateTime", "\\DateTimeImmutable");
        this.typeMapping.put("double", "float");

        this.typeMapping.put("map", "array");

        this.languageSpecificPrimitives = new HashSet<String>(Arrays.asList("bool", "boolean", "int", "integer", "double", "float", "string", "object", "\\DateTime", "\\DateTimeImmutable", "mixed", "number", "void", "byte"));
        List<String> sortedLanguageSpecificPrimitives = Arrays.asList("bool", "boolean", "int", "integer", "double", "float", "string", "mixed", "number", "void", "byte");
        Collections.sort(sortedLanguageSpecificPrimitives);
        String primitives = "'" + StringUtils.join(sortedLanguageSpecificPrimitives, "', '") + "'";
        this.additionalProperties.put("primitives", primitives);

        this.defaultIncludes.add("map");
        this.defaultIncludes.add("array");
    }

    @Override
    protected String getTemplateDir() {
        return templateDir;
    }

    @Override
    public String getDefaultTemplateDir() {
        return templateDir;
    }

    @Override
    public void processOpts() {
        super.processOpts();
        this.supportingFiles.clear();
        this.supportingFiles.add(new SupportingFile("ApiException.mustache", this.toPackagePath(this.invokerPackage, this.srcBasePath), "ApiException.php"));
        this.supportingFiles.add(new SupportingFile("Configuration.mustache", this.toPackagePath(this.invokerPackage, this.srcBasePath), "Configuration.php"));
        this.supportingFiles.add(new SupportingFile("ObjectSerializer.mustache", this.toPackagePath(this.invokerPackage, this.srcBasePath), "ObjectSerializer.php"));
        this.supportingFiles.add(new SupportingFile("ModelInterface.mustache", this.toPackagePath(this.modelPackage, this.srcBasePath), "ModelInterface.php"));
        this.supportingFiles.add(new SupportingFile("EnumInterface.mustache", this.toPackagePath(this.modelPackage, this.srcBasePath), "EnumInterface.php"));
        this.supportingFiles.add(new SupportingFile("HeaderSelector.mustache", this.toPackagePath(this.invokerPackage, this.srcBasePath), "HeaderSelector.php"));
        //this.supportingFiles.add(new SupportingFile("composer.mustache", this.getPackagePath(), "composer.json"));
        this.supportingFiles.add(new SupportingFile("README.mustache", this.getPackagePath(), "README.md"));
        this.supportingFiles.add(new SupportingFile("phpunit.xml.mustache", this.getPackagePath(), "phpunit.xml.dist"));
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        objs = super.postProcessOperations(objs);

        if (objs == null) {
            return objs;
        }

        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");

        if (operations == null) {
            return objs;
        }

        List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
        for (CodegenOperation operation : ops) {
            if (operation.returnType != null && operation.returnType.endsWith("[]")) {
                operation.returnContainer = "array";
            }
            if (!operation.getReturnTypeIsPrimitive()) {
                operation.getVendorExtensions().put("x-php-return-type", this.modelPackage() + "\\" + operation.returnType);
            }
        }

        return objs;
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        objs = super.postProcessModels(objs);

        if (objs == null) {
            return objs;
        }

        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            for (CodegenProperty var : cm.vars) {
                if (!var.getIsPrimitiveType()) {
                    var.vendorExtensions.put("x-php-type", this.modelPackage() + "\\" + var.datatype);
                }
            }
        }

        return objs;
    }

    public String getTypeDeclaration(Schema propertySchema) {
        if (!(propertySchema instanceof ArraySchema ||
                (propertySchema instanceof MapSchema)
                        && (hasSchemaProperties(propertySchema) || hasTrueAdditionalProperties(propertySchema)))
                && StringUtils.isNotBlank(propertySchema.get$ref())) {
            String schemaType = this.getSchemaType(propertySchema);
            return this.typeMapping.getOrDefault(schemaType, schemaType);
        }

        return super.getTypeDeclaration(propertySchema);
    }

    public String getTypeDeclaration(String name) {
        return name;
    }

    @Override
    public String toModelImport(String name) {
        if (this.instantiationTypes.containsKey(name)) {
            return name;
        }

        return "".equals(this.modelPackage()) ? name : this.modelPackage() + "\\" + name;
    }

    @Override
    public String toApiImport(String name) {
        return this.apiPackage() + "\\" + name;
    }
}

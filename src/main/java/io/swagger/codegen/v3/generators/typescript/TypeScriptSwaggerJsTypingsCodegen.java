package io.swagger.codegen.v3.generators.typescript;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.codegen.v3.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generates a TypeScript Typings for the sagger-js client library.
 */
public class TypeScriptSwaggerJsTypingsCodegen extends AbstractTypeScriptClientCodegen {

    private static final String GENERATOR_ID = "typescript-swagger-js-typings";
    private static final String NPM_NAME = "npmName";
    private static final String NPM_VERSION = "npmVersion";
    private static final String NPM_REPOSITORY = "npmRepository";

    public TypeScriptSwaggerJsTypingsCodegen() {
        super();
        this.cliOptions.add(new CliOption(NPM_NAME, "The name under which you want to publish generated npm package"));
        this.cliOptions.add(new CliOption(NPM_VERSION, "The version of your npm package"));
        this.cliOptions.add(new CliOption(NPM_REPOSITORY,
                "Use this property to set an url your private npmRepo in the package.json"));

        this.outputFolder = "generated-code" + File.separator + GENERATOR_ID;
        this.typeMapping.put("DateTime", "string");
    }

    @Override
    public String getName() {
        return GENERATOR_ID;
    }

    @Override
    public String getDefaultTemplateDir() {
        return GENERATOR_ID;
    }

    @Override
    public String getHelp() {
        return "Generates a TypeScript Typings for the sagger-js client library.";
    }

    @Override
    public void processOpts() {
        super.processOpts();
        if (StringUtils.isBlank(templateDir)) {
            embeddedTemplateDir = templateDir = DEFAULT_TEMPLATE_DIR + File.separator + GENERATOR_ID;
            embeddedTemplateDir = templateDir = getTemplateDir();
        }

        modelTemplateFiles.put("model.mustache", ".d.ts");
        apiTemplateFiles.put("api.interface.mustache", ".d.ts");

        apiPackage = "api";
        modelPackage = "model";

        supportingFiles.add(new SupportingFile("README.mustache", "README.md"));
        supportingFiles.add(new SupportingFile("package.mustache", "package.json"));
        supportingFiles.add(new SupportingFile("swagger-client.d.ts", "swagger-client.d.ts"));
        supportingFiles.add(new SupportingFile("index.d.ts.mustache", "index.d.ts"));
        supportingFiles.add(new SupportingFile("index.js", "index.js"));
        supportingFiles.add(new SupportingFile("api.mustache", "api.d.ts"));
        supportingFiles.add(new SupportingFile("api.json.mustache", "api.json"));

        if (!additionalProperties.containsKey(NPM_VERSION)) {
            additionalProperties.put(NPM_VERSION, "1.0.0");
        }

    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> operations) {
        operations = super.postProcessOperations(operations);
        determineApiTagName(operations);
        return operations;
    }

    private void determineApiTagName(Map<String, Object> operations) {
        Map<String, Object> objs = (Map<String, Object>) operations.get("operations");
        String apiClassName = (String) objs.get("classname");
        List<CodegenOperation> ops = (List<CodegenOperation>) objs.get("operation");
        for (CodegenOperation op : ops) {
            for (Tag tag : op.getTags()) {
                String tagApiName = toApiName(sanitizeTag(tag.getName()));
                if (tagApiName.equals(apiClassName)) {
                    operations.put("tagName", tag.getName());
                    return;
                }
            }
        }
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        ObjectMapper om = new ObjectMapper();
        om = om.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        try {
            objs.put("openAPIJson", om.writeValueAsString(objs.get("openAPI")));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return objs;
    }

    @Override
    public CodegenOperation fromOperation(String path, String httpMethod, Operation operation, Map<String, Schema> schemas, OpenAPI openAPI) {
        CodegenOperation codegenOperation = super.fromOperation(path, httpMethod, operation, schemas, openAPI);
        moveMultipartFormIntoBody(codegenOperation);
        return codegenOperation;
    }

    private void moveMultipartFormIntoBody(CodegenOperation codegenOperation) {
        for (Iterator<CodegenParameter> iterator = codegenOperation.formParams.iterator(); iterator.hasNext(); ) {
            CodegenParameter formParam = iterator.next();
            if (Objects.equals(formParam.getVendorExtensions().get(CodegenConstants.IS_MULTIPART_EXT_NAME), Boolean.TRUE)) {
                iterator.remove();
                formParam.getVendorExtensions().put(CodegenConstants.IS_BODY_PARAM_EXT_NAME, Boolean.TRUE);
                formParam.getVendorExtensions().put(CodegenConstants.IS_FORM_PARAM_EXT_NAME, Boolean.FALSE);
                codegenOperation.bodyParams.add(formParam);
            }
        }
        codegenOperation.bodyParams = addHasMore(codegenOperation.bodyParams);
        codegenOperation.formParams = addHasMore(codegenOperation.formParams);
    }
}

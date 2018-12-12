package io.swagger.codegen.v3.generators.typescript;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.codegen.v3.CliOption;
import io.swagger.codegen.v3.SupportingFile;
import io.swagger.util.Json;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Map;

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
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        try {
            objs.put("openAPIJson", Json.mapper().writeValueAsString(objs.get("openAPI")));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return objs;
    }


}

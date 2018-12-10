declare module 'swagger-client' {
    import {Spec} from 'swagger-schema-official';

    export interface SwaggerInit {
        spec: Spec;
    }

    type APIDefinition = {[key: string]: TagDef};

    export interface SwaggerClient<API extends APIDefinition> {
        apis: APIsOf<API>;
    }

    type APIsOf<API extends APIDefinition> = { [k in keyof API]: EndpointsOf<API[k]> };

    type TagDef = {[key: string]: EndpointDef};

    type EndpointsOf<Tags extends TagDef> = { [k in keyof Tags]: RequestOf<Tags[k]> };

  type RequestOptions<RequestBody> = {
    requestBody?: RequestBody;
    server?: string;
    serverVariables?: { [key: string]: string };
  };

  type EndpointDef<Request extends Object = {}, RequestBody extends Object = {}, Response extends Object = {}> = (
    request: Request,
    options?: RequestBody
  ) => Response;

  type Response<Def extends { status: number; body: any }> = Def & {
        ok: string;
    };

  type Init<T> = T extends EndpointDef<infer Request, any, any> ? Request : never;
  type Options<T> = T extends EndpointDef<any, infer RequestBody, any> ? RequestBody : never;

  type RequestOf<Endpoint extends EndpointDef> = (
    init: Init<Endpoint>,
    options?: RequestOptions<Options<Endpoint>>
  ) => Promise<Response<ReturnType<Endpoint>>>;

    export function Swagger<T extends APIDefinition>(init: SwaggerInit): Promise<SwaggerClient<T>>;

    export default Swagger;
}
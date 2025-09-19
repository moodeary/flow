package flow.common.config;


import flow.common.dto.ResponseApi;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ResponseEntity, ResponseApi, 또는 Spring 관련 클래스를 반환하는 경우는 제외
        String typeName = returnType.getParameterType().getSimpleName();
        String packageName = returnType.getDeclaringClass().getPackageName();

        return !typeName.equals("ResponseApi") &&
               !typeName.equals("ResponseEntity") &&
               !packageName.startsWith("org.springframework") &&
               !packageName.startsWith("springfox") &&
               !packageName.startsWith("org.springdoc");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 이미 ResponseApi인 경우 그대로 반환
        if (body instanceof ResponseApi) {
            return body;
        }

        // 일반 객체를 ResponseApi로 감싸서 반환
        return ResponseApi.success(body);
    }
}
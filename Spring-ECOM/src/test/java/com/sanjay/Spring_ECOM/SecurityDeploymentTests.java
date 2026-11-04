package com.sanjay.Spring_ECOM;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityDeploymentTests {
 @LocalServerPort int port;
 HttpResponse<String> get(String path,String origin) throws Exception {
  var b=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path));if(origin!=null)b.header("Origin",origin);
  return HttpClient.newHttpClient().send(b.build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test void corsUsesOnlyConfiguredOrigin() throws Exception {
  var allowed=get("/api/products","http://localhost:3000");assertEquals(200,allowed.statusCode());assertEquals("http://localhost:3000",allowed.headers().firstValue("access-control-allow-origin").orElseThrow());
  assertEquals(403,get("/api/products","https://untrusted.example").statusCode());
 }
 @Test void anonymousCannotSeeCustomersOrManageStore() throws Exception {
  assertEquals(401,get("/api/orders",null).statusCode());assertEquals(401,get("/api/me",null).statusCode());
  var req=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api/product/1")).header("X-Requested-With","XMLHttpRequest").DELETE().build();assertEquals(401,HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString()).statusCode());
  assertEquals(200,get("/actuator/health",null).statusCode());
 }
}

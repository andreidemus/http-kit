# HTTP Kit

Contains:
- Requests (a very basic Java HTTP client for humans, inspired by Python's [Requests](http://docs.python-requests.org/en/master/) library.)
- Responses (a very basic Java HTTP server for http clients testing and debugging)

**Library is not stable yet, please, use with care**


## Requests


### Usage


#### Example 1
```java
final String responseBody = Requests.get("https://api.github.com/zen").text();
System.out.println(responseBody);
```
```
> Speak like a human.
```

#### Example 2
```java
final Request request = new Request("http://httpbin.org").path("post")
                                                         .header("Content-Type", "application/json")
                                                         .pathParam("a", 5)
                                                         .body("{\"x\":10}");
final Response response = Requests.post(request);
System.out.println(response);
```

```
> Status: 200
Reason: OK
Headers:
{
  X-Processed-Time : [0.000765085220337]
  Server : [meinheld/0.6.1]
  Access-Control-Allow-Origin : [*]
  Access-Control-Allow-Credentials : [true]
  Connection : [keep-alive]
  Content-Length : [444]
  Date : [Thu, 07 Sep 2017 19:05:42 GMT]
  Content-Type : [application/json]
  X-Powered-By : [Flask]
  Via : [1.1 vegur]
}
Body:
{
  "args": {
    "a": "5"
  }, 
  "data": "{\"x\":10}", 
  "files": {}, 
  "form": {}, 
  "headers": {
    "Accept": "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2", 
    "Connection": "close", 
    "Content-Length": "8", 
    "Content-Type": "application/json", 
    "Host": "httpbin.org", 
    "User-Agent": "Java/1.8.0_112"
  }, 
  "json": {
    "x": 10
  }, 
  "origin": "178.210.135.192", 
  "url": "http://httpbin.org/post?a=5"
}

```

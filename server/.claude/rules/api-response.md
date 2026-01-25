# API 응답 패턴 규칙

## 필수 규칙

모든 REST API 응답은 `SuccessResponse<T>`로 래핑해야 합니다.

### 응답 패턴

```java
HttpStatus status = HttpStatus.XXX;
return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
```

### 예시

```java
// 데이터 반환 (200 OK)
@GetMapping
public ResponseEntity<SuccessResponse<List<MyResponse>>> getList() {
    List<MyResponse> response = service.getList();
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}

// 생성 (201 Created)
@PostMapping
public ResponseEntity<SuccessResponse<CreateResponse>> create(@RequestBody CreateRequest request) {
    CreateResponse response = service.create(request);
    HttpStatus status = HttpStatus.CREATED;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}

// Void 응답 (204 No Content)
@DeleteMapping("/{id}")
public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long id) {
    service.delete(id);
    HttpStatus status = HttpStatus.NO_CONTENT;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, null));
}
```

### 응답 구조

```json
{
  "code": 200,
  "status": "OK",
  "data": { ... }
}
```

## 참조 파일

- **SuccessResponse**: `src/main/java/moment/global/dto/response/SuccessResponse.java`

## 금지 사항

- ❌ `ResponseEntity<T>` 직접 반환 (SuccessResponse 래핑 없이)
- ❌ `ResponseEntity.ok(response)`
- ❌ `ResponseEntity.noContent().build()`
- ❌ `ResponseEntity.status(status).body(response)` (SuccessResponse 없이)

## 테스트 코드 패턴

테스트에서 응답 추출 시 `data` 필드에서 추출해야 합니다.

```java
// 단일 객체
.extract()
.jsonPath()
.getObject("data", MyResponse.class);

// 리스트
.extract()
.jsonPath()
.getList("data", MyResponse.class);

// 문자열
.extract()
.jsonPath()
.getString("data");
```

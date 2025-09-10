### 📝 **BE 코드 컨벤션**

#### **1️⃣ 네이밍 & 구조 컨벤션**

##### **1.1. 패키지 구조**

패키지는 **도메인을 기준**으로 구성하며, 각 도메인 패키지 내부는 계층형 아키텍처를 따릅니다. 공통 로직을 담는 `global` 패키지를 활용하며, `util` 패키지의 위치는 팀의 논의를 통해 결정합니다.

```plainText
com.example.project
└── global
├── dto
│   ├── request
│   └── response
├── exception
└── config
└── member
├── presentation
├── application
├── domain
├── infrastructure
├── dto
│   ├── request
│   └── response
├── exception
└── config

```

##### **1.2. 네이밍 규칙**

- **클래스 (Class)**: 명사 형태의 **UpperCamelCase**를 사용합니다. (예: `UserEmail`)
- **메서드 (Method)**: 동사로 시작하는 **camelCase**를 사용하며, 객체 이름을 중복해서 넣지 않습니다. (예: `line.getLength()` O, `line.getLineLength()`
  X)
- **변수 (Variable)**: **camelCase**를 기본으로 합니다. (예: `userEmail`)
- **상수 (Constant & Enum)**: **UPPER_SNAKE_CASE**를 사용합니다. (예: `NORMAL_STATUS`)
- **패키지 (Package)**: 전체 **소문자**로 작성합니다. (예: `useremail`)
- **URL & 파일 (URL & File)**: **kebab-case**를 사용합니다. (예: `/user-email-page`)
- **컬렉션 (Collection)**: 복수형 명사를 사용하고, 이름에 `List` 등 컬렉션 타입을 명시하지 않습니다. (예: `List<Member> members`)

---

#### **2️⃣ 클래스별 컨벤션**

##### **2.1. Controller**

- 클래스명은 `Controller` 접미사로 끝납니다. (예: `MemberController` O, `MemberApiController` X)
- 반환 타입으로 **`ResponseEntity`**를 사용합니다.
- Controller와 Service의 기본 메서드 네이밍은 다음 규칙을 따릅니다.

| 요청       | Controller  | Service              |
|----------|-------------|----------------------|
| 목록 조회    | `readXXX`   | `getXXXs`, `findXXX` |
| 단건 상세 조회 | `readXXX`   | `getXXX`, `findXXX`  |
| 등록       | `createXXX` | `addXXX`             |
| 수정       | `updateXXX` | `modifyXXX`          |
| 삭제       | `deleteXXX` | `removeXXX`          |

##### **2.2. Service**

- **`get`**: 반드시 존재해야 하는 데이터를 조회할 때 사용하며, 예외가 발생하지 않거나 NPE 가능성이 있습니다. (예: `getUserId()`)
- **`find`**: 데이터 존재 여부가 불확실할 때 사용하며, `Optional` 또는 `null`을 반환합니다. (예: `findUserById()`)
- **`read`**: 외부 시스템(DB, File 등)의 데이터를 읽을 때 사용하며, Side Effect가 없습니다. (예: `readFromDB()`)

##### **2.3. Domain & Entity**

- `equals()`, `hashCode()`, `toString()`을 반드시 재정의합니다.
- `equals()`는 **id 필드만을 비교**하여 구현합니다.
- ID의 타입은 **`Long`**을 사용합니다.

##### **2.4. DTO (Data Transfer Object)**

- **Request DTO**
    - `Controller`에서 `Service` 계층까지 전달됩니다.
    - 유효성 검증 로직(`@NotNull` 등)을 포함합니다.
    - `Service`에서 Entity로 변환되며, 변환 로직은 Request DTO 내에 포함될 수 있습니다.
- **Response DTO**
    - `Service`에서 생성하여 `Controller`로 전달됩니다.
- **정적 팩토리 메서드**
    - DTO 내부에서 정적 팩토리 메서드 활용을 권장합니다.
    - `from()`: 파라미터가 1개일 경우 사용합니다.
    - `of()`: 파라미터가 2개 이상일 경우 사용합니다.

---

#### **3️⃣ 코드 스타일 및 포맷팅**

##### **3.1. 코드 스타일**

- IntelliJ Code Style은 **우아한테크코스 스타일 가이드**를 따릅니다.

##### **3.2. 멤버 순서**

- **접근 제어자**: `static` → `public` → `private` 순으로 작성합니다.
- `public` 메서드와 관련된 `private` 메서드는 바로 아래에 배치합니다.
- 여러 곳에서 사용되는 `private` 메서드는 클래스 하단에 모아서 배치합니다.

##### **3.3. 어노테이션 순서**

- 핵심 역할 정의 (`@Controller`, `@Service`, `@Entity`)
- 핵심 기능 설정 (`@Transactional`, `@RequestMapping`)
- 유효성 검증 (`@Valid`, `@NotNull`)
- 생성 관련 (`@Builder`, `@AllArgsConstructor`)
- 문서화/테스트 (`@DisplayName`)

##### **3.4. Lombok 가이드라인**

- 객체의 무분별한 수정을 방지하기 위해 **`@Setter`와 `@Data` 어노테이션의 사용은 지양**합니다.

---

#### **4️⃣ 테스트 전략** 🧪

##### **4.1. 테스트 원칙**

- 테스트 코드는 **BDD(Behavior-Driven Development) 스타일**을 따릅니다. `given / when / then` 구조를 명확히 합니다.

```java
// given - 상황 설정

// when - 행동 실행

// then - 결과 검증
```

##### **4.2. 계층별 테스트**

- Domain: 외부 의존성 없이 순수한 단위 테스트로 작성합니다.

- Repository: JpaRepository 기본 제공 메서드는 테스트하지 않습니다. 직접 작성한 쿼리(@Query)나 복잡한 쿼리 메서드는 H2 데이터베이스를 이용해 테스트합니다.

- Service: 외부 의존성(Repository 등)은 모킹(Mocking)하여 단위 테스트를 진행합니다.

- E2E Test: API의 정상 및 예외 시나리오를 모두 검증하기 위해 RestAssured를 활용한 E2E 테스트를 작성합니다.
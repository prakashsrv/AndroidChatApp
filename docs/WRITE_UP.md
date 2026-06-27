# Write-Up — Real-Time Chat Thread

---

# 1. What I asked the AI to do vs. what I decided myself

I treated AI as a development partner rather than an autopilot. Before writing any code, I spent time deciding the architecture and documenting it in a pre-implementation analysis. That document became the blueprint for everything that followed.

### Decisions I made myself

* Native Android using **Kotlin + Jetpack Compose**
* A **UseCase-heavy Clean Architecture** with MVVM
* Room as the **single source of truth** for every message
* Stable optimistic ordering using **client timestamps**
* A fakeable `ChatMessageStream` abstraction so the UI never depends on a concrete stream implementation
* Unidirectional data flow throughout the feature
* Small, reviewable commits instead of one large feature commit

Once those decisions were made, implementation became much more mechanical.

### Where AI helped

I used AI primarily to speed up repetitive work:

* Scaffolding the project structure
* Creating Room entities, DAOs and Hilt modules
* Building Compose UI components such as `MessageBubble`, `TypingIndicator` and `ChatInputBar`
* Generating implementation code one module at a time
* Writing the majority of the unit tests
* Setting up linting and formatting

I never generated the entire application in one prompt. Every module was generated independently, reviewed, adjusted where necessary, then manually tested before moving on to the next.

---

# 2. Where I overrode or corrected the AI

The most valuable part of using AI wasn't accepting its output—it was knowing when not to.

### Runtime crash

The generated implementation left a `TODO()` inside `sendMessageToServer()`. Everything compiled, but the app crashed as soon as I pressed **Send**. I caught it during manual testing and replaced the placeholder implementation.

### Debug controls

The AI initially placed the debug controls underneath the input bar, making them impossible to interact with. This was only obvious after running the app, so I moved the panel above the composer.

### Duplicate message reconciliation

Initially the AI treated the optimistic UUID as the permanent identifier. That looked correct until I considered what happens when the server acknowledges the message using a different ID.

That would leave the pending row in the database while inserting another confirmed row, creating duplicate messages.

Instead, I kept the original local row, updated its status, and stored the server identifier separately in `serverId`. The optimistic message simply evolves into the confirmed one.

### Coroutine lifetime

The repository originally created its own `CoroutineScope`, which had no clear owner and nothing responsible for cancelling it.

I replaced this with an injected `@ApplicationScope` so the coroutine lifetime becomes explicit and consistent with the rest of the application.

---

# 3. Biggest trade-offs

## Room as the Single Source of Truth

Every message—optimistic or inbound—flows through Room before reaching the UI.

### Alternative

Keep an in-memory list inside the ViewModel and merge optimistic sends with incoming stream events there.

### Why I chose Room

Having a single pipeline means ordering, reconciliation and deduplication all happen in one place. The UI simply observes a database Flow instead of maintaining its own copy of state.

### What I gained

* Predictable state management
* Automatic lifecycle handling
* Process death recovery
* Easier debugging
* Simpler testing

### What I gave up

* Every message requires a database write
* Slightly higher latency than pure in-memory updates
* Higher write pressure during very large bursts

For a chat application, I think the consistency is worth the small performance cost.

---

## Client timestamps instead of server timestamps

Messages sent by the current user are always ordered using their original `clientTimestamp`.

### Alternative

Replace the timestamp with the server timestamp once the acknowledgement arrives.

### Why I chose client timestamps

Changing to the server timestamp after acknowledgement causes messages to visibly jump around whenever network latency is inconsistent.

Keeping the original client timestamp means the message stays exactly where the user first saw it.

### What I gained

* Stable ordering
* No visual jumping
* Better perceived responsiveness

### What I gave up

* Client clocks are not perfectly accurate
* I need to maintain both client and server timestamps

For user experience, stable ordering felt more important than perfectly accurate chronology.

---

## UseCase-heavy Clean Architecture

Every business operation lives in its own UseCase.

### Alternative

Move the business logic directly into the ViewModel.

### Why I chose it

The assignment focuses heavily on correctness, predictable state and testing. Keeping the business logic independent from Android makes every rule easy to unit test.

### What I gained

* Small ViewModels
* Pure Kotlin business logic
* Easy unit testing
* Better scalability as more features are added

### What I gave up

* More files
* More dependency injection wiring
* Slightly slower initial development

For a production codebase, I'd take the additional structure over slightly less boilerplate.

---

## Stream abstraction instead of direct WebSocket usage

The ViewModel depends on a `ChatMessageStream` interface instead of any concrete implementation.

### Alternative

Expose WebSocket callbacks directly to the presentation layer.

### Why I chose it

The same ViewModel works with today's fake implementation and tomorrow's real WebSocket without changing any presentation code.

It also makes testing straightforward because the stream can be replaced with a deterministic fake.

The extra abstraction adds a little more setup, but it keeps the architecture loosely coupled.

---

# 4. What I'd improve with another day

If I had another day, I'd focus less on adding features and more on making the feature production ready.

* Connect `ShowError` to an actual Snackbar instead of only emitting the effect.
* Add Paging 3 with a `RemoteMediator` so old conversations load incrementally.
* Replace the fake server with a local mock server to validate the networking flow end-to-end.
* Add integration tests covering the full retry path instead of only unit testing the business logic.
* Simulate larger message bursts to profile database throughput and scrolling performance.

---

# 5. State & performance rationale

## Why StateFlow + SharedFlow

`StateFlow` represents long-lived UI state. It always has a current value, survives configuration changes and is straightforward to test.

`SharedFlow` is used for one-time events such as scrolling, snackbars and navigation. These shouldn't replay when the screen recomposes.

I considered `LiveData`, but Flow integrates better with Room, Coroutines and Compose while remaining platform independent.

---

## Techniques keeping the UI responsive

* `collectAsStateWithLifecycle()` avoids unnecessary work while the screen is stopped.
* `LazyColumn` uses stable keys so unchanged rows don't recompose during bursts.
* Room emits only actual database changes.
* `@Upsert` avoids extra read-before-write queries.
* All long-running work stays off the main thread using coroutines.

---

## Where this architecture starts breaking down

The current implementation writes each inbound message individually.

Under very high throughput, sequential database writes become the bottleneck.

The first optimisation would be batching writes inside a single Room transaction.

The second limitation is loading every message into memory. Once conversations become very large, the current `ORDER BY clientTimestamp` query no longer scales well. At that point I'd switch to Paging 3 so only the visible portion of the conversation is loaded.

---

# 6. Architecture as a standard

If this became the reference implementation for a larger application, these are the conventions I'd expect every feature to follow.

## Conventions

* Every feature owns its own `data`, `domain` and `presentation` layers.
* ViewModels orchestrate—they don't contain business logic.
* Every business operation has a dedicated UseCase.
* Repository interfaces belong in the domain layer.
* Every external data source sits behind an interface.
* Room remains the single source of truth whenever offline support is required.
* One-shot UI events always use `SharedFlow`.
* Long-lived coroutines use injected application scopes instead of unmanaged `CoroutineScope`s.

## Rules I'd automate

* Prevent ViewModels from importing anything from the data layer.
* Generate new features with the three-layer structure by default.
* Require UseCases to remain Android-free.
* Enforce Detekt and ktlint in CI before every merge.


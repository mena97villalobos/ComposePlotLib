# Security Policy

## Supported versions

Security fixes land on the latest released minor version. Older versions are not patched.

| Version | Supported |
|---------|-----------|
| 1.2.x   | ✅        |
| < 1.2   | ❌        |

## Reporting a vulnerability

Please report privately via
[GitHub Security Advisories](https://github.com/mena97villalobos/ComposePlotLib/security/advisories/new)
rather than opening a public issue.

Expect an initial response within 7 days. If a fix is warranted, it will ship in the next patch
release and you will be credited in the advisory unless you prefer otherwise.

## Threat model — please read before reporting

ComposePlotLib is a rendering library. It performs **no network I/O, no file I/O, no
deserialization, and no format parsing**. It receives already-constructed, in-memory, immutable
specs from the host application and turns them into draw commands.

That makes the realistic vulnerability class **availability, not confidentiality**. Concretely, the
reports that make sense here are:

- Unbounded allocation, a non-terminating loop, or pathological slowdown in planning, decimation, or
  tick generation, triggered by hostile or degenerate input (huge point counts, `NaN`, infinities,
  extreme magnitudes, zero-span domains) — hanging or OOM-ing the host app's UI thread.
- A crash reachable from data an application would plausibly pass through from an untrusted source.

Out of scope, because the library does not do these things: injection of any kind, authentication
and authorization, secrets handling, TLS, and data exfiltration. Also out of scope are
vulnerabilities in the host application's own handling of chart data before it reaches us.

If you are unsure whether something qualifies, report it privately anyway — a hang triggered by
attacker-influenced data is a real bug even when the impact is limited.

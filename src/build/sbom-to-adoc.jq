# Renders the CycloneDX SBOM as a human-readable AsciiDoc page (docs/sbom.adoc).
#
# Invoked from pom.xml during the "package" phase, see the "sbom" profile.
#
# The output is deliberately deterministic - the SBOM's timestamp and serialNumber are
# NOT included, so docs/sbom.adoc only changes when the dependencies actually change,
# rather than producing a git diff on every build.

# A component may carry several license entries (Jetty and jakarta.servlet-api are
# dual-licensed), and each entry is either an SPDX id, a free-text name, or an expression.
def licenses:
    [ (.licenses // [])[]
      | if   .expression   then .expression
        elif .license.id   then .license.id
        elif .license.name then .license.name
        else "unknown" end ]
    | if length == 0 then "_not stated_" else join(", ") end;

def coordinates:
    if .group then .group + ":" + .name else .name end;

[
    "= Third-Party Dependencies",
    ":toc:",
    "",
    "Generated from the CycloneDX SBOM by `mvn package` -- *do not edit by hand*.",
    "The machine-readable SBOM is built to `target/" + $sbomFile + "`;",
    "see the Packaging section of CLAUDE.md.",
    "",
    "Product:: " + $productName + " " + (.metadata.component.version // ""),
    "Supplier:: " + (.metadata.supplier.name // "_not stated_"),
    "Product license:: " + (.metadata.component | licenses),
    "SBOM format:: CycloneDX " + .specVersion,
    "Components:: " + (.components | length | tostring),
    "",
    "== Components",
    "",
    "Components listing more than one license are multi-licensed; the SBOM records the",
    "declared licenses without asserting which one applies.",
    "",
    "[cols=\"4,1,3\",options=\"header\"]",
    "|===",
    "| Component | Version | License"
]
+ (
    [ .components[]
      | "| " + coordinates + " | " + (.version // "") + " | " + licenses
    ] | sort
)
+ [
    "|==="
]
| join("\n")

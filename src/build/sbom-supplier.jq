# Adds the supplier / author fields that cyclonedx-maven-plugin cannot set itself
# (it has no config parameter for them, verified against plugin 2.9.2).
# BSI TR-03183-2 requires supplier information; a natural person is a valid supplier.
#
# Invoked from pom.xml during the "package" phase with --arg name/url/email.

def supplier:
    {
        name:    $name,
        url:     [$url],
        contact: [{name: $name, email: $email}]
    };

  .metadata.supplier           = supplier
| .metadata.authors            = [{name: $name, email: $email}]
| .metadata.component.supplier = supplier

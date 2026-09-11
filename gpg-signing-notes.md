# GPG Signing Setup for Maven Central Publishing

#. Generate a key

  ```bash
  gpg --full-generate-key
  ```
#. List keys

  ```bash
  gpg --list-secret-keys --keyid-format=long
  ```
  Shows every secret key in your local keystore, e.g.:
  ```
  sec   rsa4096/8F363F3EAEFAFD59 2026-09-10 [SC]
        2E34BE2720B1EE91F3C87D1F8F363F3EAEFAFD59
  uid                 [ultimate] Odys Z (sign jars) <odys.zhou@gmail.com>
  ssb   rsa4096/588714193DED41DA 2026-09-10 [E]
  ```

  Check which keystore/homedir is active if results look unexpected:
  ```bash
  gpgconf --list-dirs homedir
  echo $GNUPGHOME
  ```

#. Upload the public key, and verify

  Central supports three keyservers — push to more than one for redundancy:
  ```bash
  gpg --keyserver keyserver.ubuntu.com --send-keys <FINGERPRINT>
  gpg --keyserver keys.openpgp.org     --send-keys <FINGERPRINT>
  gpg --keyserver pgp.mit.edu          --send-keys <FINGERPRINT>
  ```

  Verify propagation before relying on it:
  ```bash
  gpg --keyserver keys.openpgp.org --recv-keys <FINGERPRINT>
  ```
  `keyserver.ubuntu.com` is a multi-node pool that can be inconsistent.

#. Point Maven at the right key — `~/.m2/settings.xml`

  ```xml
  <settings>
    <profiles>
      <profile>
        <id>gpg-sign</id>
        <properties>
          <gpg.keyname>2E34BE2720B1EE91F3C87D1F8F363F3EAEFAFD59</gpg.keyname>
        </properties>
      </profile>
    </profiles>

    <activeProfiles>
      <activeProfile>gpg-sign</activeProfile>  <!-- easy to forget! -->
    </activeProfiles>

    <servers>
    ...
    </servers>
  </settings>
  ```

  `maven-gpg-plugin` reads `gpg.keyname` automatically — no `<keyname>` needed in the pom itself.

  **Verify it's actually picked up before deploying:**
  ```bash
  mvn help:evaluate -Dexpression=gpg.keyname -q -DforceStdout
  ```
  Should print the fingerprint.

  **Confirm which key actually signed a build's artifacts**, if in doubt:
  ```bash
  gpg --verify antson-1.1.0.jar.asc antson-1.1.0.jar
  ```

## Pitfalls hit so far

- Multiple secret keys in one keystore → GPG's default-key pick is ambiguous; always pin `gpg.keyname`.

- A defined-but-not-activated settings.xml profile is silently ignored — always `mvn help:evaluate` to confirm before trusting a deploy.

- Signature is only valid to Central once the *specific signing key* is confirmed reachable on a supported keyserver — not just "a" key you generated.

# About

Antson java version.

# Troubleshooting for maven deploy

```
[ERROR] Rule failure while trying to close staging repository with ID "iogithubodys-z-xxxx".
[ERROR]
[ERROR] Nexus Staging Rules Failure Report
[ERROR] ==================================
[ERROR]
[ERROR] Repository "iogithubodys-z-xxxx" failures
[ERROR]   Rule "signature-staging" failures
[ERROR]     * No public key: Key with id: (###############) was not able to be located on &lt;a href=http://keyserver.ubuntu.com:11371/&gt;http://keyserver.ubuntu.com:11371/&lt;/a&gt;. Upload your public key and try the operation again.
[ERROR]     * No public key: Key with id: (###############) was not able to be located on &lt;a href=https://keys.openpgp.org/&gt;https://keys.openpgp.org/&lt;/a&gt;. Upload your public key and try the operation again.
```

See [Stackoverflow answer](https://stackoverflow.com/a/32962786/7362888)

```
    gpg --list-keys
```

Upload to one of the above server.

```
    gpg --keyserver hkp://keyserver.ubuntu.com --send-keys xxxx
```

or update via www front end, like

```
    https://keys.openpgp.org
```

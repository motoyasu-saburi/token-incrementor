# IncrementItPlease
A simple but useful Burp extension to increment a parameter in each request, intended for use with Active Scan.

An example use case would be if  you are active scanning a "create user" form, which would normally produce an error if you created two users with the same username. You can use the text "IncrementItPlease" for the username parameter parameter and it will replace it with "Incremented[RandomInt][Counter]", so that you can successfully active scan this form.

## Example
It will match:
```
{"name":"hogehogeIncrementItPlease:123"}
```
And replace it with:
```
{"name":"hogehoge123"}
{"name":"hogehoge124"}
...
{"name":"hogehoge9999"}
```

## Releases
See the [Releases](https://github.com/motoyasu-saburi/token-incrementor) tab for a pre-built jar.

## Build and Register
```
$ ./gradlew wrapper
$ ./gradlew tasks
$ ./gradlew shadowJar

Register ./build/libs/token-incrementor-all.jar to Burp Extender
```


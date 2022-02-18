# JavaStringExternalize

A basic internationalization tool to externalize strings in Java source files so that they can be translated. It's heavily inspired by Eclipse's Externalize Strings tool, but I want to externalize my strings in a specific way since I'm transcompiling to JavaScript using GWT, so I've written my own tool so that I could customize the output myself. The code is written specifically to handle my desired way to access external strings, which is fairly unique, so you will need to change the code to output a more standard format

![Screenshot of UI](docs/stringExternalizationUi.png)


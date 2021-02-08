Генератор шаблонного исходного кода проектов.

Пока что из командной строки поддерживается (по умолчанию) генерация crud-проекта client-react из шаблонов mustache.

Пример запуска из Windows/cmd
```
java -jar "C:\work\bin-ext\build\org\jepria\tools\generator\generator\1.1.0-SNAPSHOT\generator-1.1.0-SNAPSHOT.jar" --api-specs "C:\work\TargetProject\App\service-rest\src\api-spec\entity\swagger.json" --output-root "C:\work\TargetProject\App\client-react"
```

По умолчанию шаблоны для генерации берутся из ресурсов внутри jar. Но можно указать кастомные наборы шаблонов для генерации, расположенные локально:
```
java -jar "C:\work\bin-ext\build\org\jepria\tools\generator\generator\1.1.0-SNAPSHOT\generator-1.1.0-SNAPSHOT.jar" --api-specs "C:\work\TargetProject\App\service-rest\src\api-spec\entity\swagger.json" --output-root "C:\work\TargetProject\App\client-react" --template-root-mst "C:\work\templates\mustache-templates\client-react\crud\ROOT" --partials-root-mst "C:\work\templates\mustache-templates\client-react\partials"
```

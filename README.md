Генератор шаблонного исходного кода проектов.

Пока что из командной строки поддерживается (по умолчанию) генерация crud-проекта client-react из шаблонов mustache.

Пример запуска из Windows/cmd
```
java -jar "C:\work\bin-ext\build\org\jepria\tools\generator\generator\1.2.0-SNAPSHOT\generator-1.2.0-SNAPSHOT.jar" --api-specs "C:\work\TargetProject\service-rest\src\api-spec\entity\swagger.json" --output-root "C:\work\TargetProject\client-react" --entity-name main-entity
```

Опция `--entity-name` указывает, для какой сущности генерировать код, и (в случае если сущность дочерняя) отражает связи с родительскими сущностями. Примеры значений:
```
--entity-name main-entity
--entity-name /main-entity/child-entity
```

По умолчанию шаблоны для генерации берутся из ресурсов внутри jar. Но можно указать кастомные наборы шаблонов для генерации, расположенные локально, при помощи опций:
```
--template-root-mst "C:\work\templates\mustache-templates\client-react\crud\ROOT" --partials-root-mst "C:\work\templates\mustache-templates\client-react\partials"
```

## Генератор шаблонного исходного кода проектов.

#### Генерация клиентской части scrud-проекта (client-react) из API-спецификации swagger.
Требования к api-спецификации

Наличие хотя бы некоторых SCRUD-методов сущности, помеченных соответствующими `operationId`:
```
"operationId": "entity-name:search",
"operationId": "entity-name:create",
"operationId": "entity-name:read",
"operationId": "entity-name:update",
```
или, в случае с иерархией сущностей "родительская-подчинённая":
```
"operationId": "/parent-entity/child-entity:search",
"operationId": "/parent-entity/child-entity:create",
"operationId": "/parent-entity/child-entity:read",
"operationId": "/parent-entity/child-entity:update",
```

Пример запуска из Windows/cmd для генерации кода родительской сущности
```
java -jar "C:\work\bin-ext\build\org\jepria\tools\generator\generator\1.2.0-SNAPSHOT\generator-1.2.0-SNAPSHOT.jar" --api-specs "C:\work\TargetProject\service-rest\src\api-spec\entity\swagger.json" --output-root "C:\work\TargetProject\client-react" --entity-name main-entity
```
Опция `--entity-name` указывает, для какой сущности генерировать код

После генерации требуется ручное редактирование ролей доступа к приложению и текстовых ресурсов (контекстным поиском слова `MRT_` по сгенерированному коду)

Пример запуска из Windows/cmd для генерации кода дочерней сущности
```
java -jar "C:\work\bin-ext\build\org\jepria\tools\generator\generator\1.2.0-SNAPSHOT\generator-1.2.0-SNAPSHOT.jar" --api-specs "C:\work\TargetProject\service-rest\src\api-spec\entity\swagger.json" --output-root "C:\work\TargetProject\client-react" --entity-name /parent-entity/child-entity
```

После генерации требуется ручное редактирование текстовых ресурсов приложения и связка дочернего модуля с родительским согласно инструкциям, выведенным в консоль после окончания генерации:

```
====== The following code parts must be added manually into existing files ======
1) C:\work\TargetProject\client-react\src\app\store.ts

import childEntityReducer from "../features/childEntity/state/childEntitySlice";

childEntity: childEntityReducer,

2) C:\work\TargetProject\client-react\src\features\parentEntity\ParentEntityModuleRoute.tsx

import ChildEntityRoute from "../childEntity/ChildEntityRoute";

<Route path={`${path}/:parentEntityId/child-entity`}>
  <ChildEntityRoute/>
</Route>

3) C:\work\TargetProject\client-react\src\features\parentEntity\ParentEntityRoute.tsx

{currentRecord?.parentEntityId ? (
  <Tab
      selected={false}
      onClick={() => {
        history.push(`/parent-entity/${currentRecord?.parentEntityId}/child-entity/list`);
      }}
  >
    {t("childEntity.header")}
  </Tab>
) : null}
```

#### Настройки

По умолчанию шаблоны для генерации берутся из ресурсов внутри jar. Но можно указать кастомные наборы шаблонов для генерации, расположенные локально, при помощи опций:
```
--template-root-mst "C:\work\templates\mustache-templates\client-react\crud\ROOT" --partials-root-mst "C:\work\templates\mustache-templates\client-react\partials"
```

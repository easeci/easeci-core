package io.easeci.core.workspace.vars;

import io.easeci.BaseWorkspaceContextTest;
import io.easeci.core.workspace.LocationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static io.easeci.extension.command.VariableType.NUMBER;
import static io.easeci.extension.command.VariableType.STRING;
import static org.junit.jupiter.api.Assertions.*;

class GlobalVariablesManagerTest extends BaseWorkspaceContextTest {

    @BeforeEach
    void setupEach() {
        // singleton - clear variables in map in order to make tests works well
        GlobalVariablesManager.getInstance().clear();
    }

    @Test
    @DisplayName("Should correctly create vars file in workspace")
    void creationEmptyVarsFileTest() {
        GlobalVariablesManager.getInstance();

        Path varsFileLocation = LocationUtils.getVarsFileLocation();

        assertTrue(Files.exists(varsFileLocation));
    }

    @Test
    @DisplayName("Should correctly store variable in vars file")
    void createVariableTest() {
        final String variableName = "name";
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = Variable.of(STRING, variableName, "EaseCI");

        Variable<String> added = vars.put(var);
        Optional<Variable<String>> optionalVariable = vars.get(variableName);

        assertAll(() -> assertNotNull(added),
                  () -> assertEquals(var, added),
                  () -> assertTrue(optionalVariable.isPresent()),
                  () -> assertEquals(1, vars.variableSize()));
    }

    @Test
    @DisplayName("Should reject variable which name exists")
    void createVariablesExistsTest() {
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = Variable.of(STRING, "name", "EaseCI");

        Variable<String> added = vars.put(var);

        assertThrows(IllegalStateException.class, () -> vars.put(var));
    }

    @Test
    @DisplayName("Should reject variable when it is null")
    void createVariableNullTest() {
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = null;

        assertThrows(IllegalStateException.class, () -> vars.put(var));
    }

    @Test
    @DisplayName("Should reject variable when value is null but wrapping object is initialized")
    void createVariableValueNullTest() {
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = Variable.of(STRING, "name", null);

        assertThrows(IllegalStateException.class, () -> vars.put(var));
    }

    @Test
    @DisplayName("Should reject variable when it has illegal characters in name")
    void createVariableIllegalCharsTest() {
        final String illegalName = "$variable";
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = Variable.of(STRING, illegalName, "EaseCI");

        assertThrows(IllegalArgumentException.class, () -> vars.put(var));
    }

    @Test
    @DisplayName("Should correctly find and return variable")
    void findVariableTest() {
        final String variableName = "name";
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = Variable.of(STRING, variableName, "EaseCI");

        Variable<String> added = vars.put(var);

        Optional<Variable<String>> found = vars.get(variableName);

        assertAll(() -> assertNotNull(added),
                () -> assertEquals(var, added),
                () -> assertNotNull(found),
                () -> assertEquals(added, found.get()));
    }

    @Test
    @DisplayName("Should not found variable, return empty Optional object")
    void findVariableNotFoundTest() {
        final String notExistingVariableName = "name-not-exists";
        GlobalVariables vars = GlobalVariablesManager.getInstance();

        Optional<Variable<String>> found = vars.get(notExistingVariableName);

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should correctly remove variable and return this one")
    void removeVariableTest() {
        final String variableName = "name";
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = Variable.of(STRING, variableName, "EaseCI");

        Variable<String> added = vars.put(var);
        Optional<Variable<String>> optionalVariable = vars.get(variableName);

        assertAll(() -> assertNotNull(added),
                () -> assertEquals(var, added),
                () -> assertTrue(optionalVariable.isPresent()));

        Optional<Variable<String>> removedOptional = vars.remove(variableName);

        Optional<Variable<String>> variableNotExist = vars.get(variableName);

        assertAll(() -> assertTrue(removedOptional::isPresent),
                  () -> assertEquals(added, removedOptional.get()),
                  () -> assertFalse(variableNotExist.isPresent()),
                  () -> assertEquals(0, vars.variableSize()));
    }

    @Test
    @DisplayName("Should correctly edit variable")
    void editVariableTest() {
        final String variableName = "name";
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = Variable.of(STRING, variableName, "EaseCI");

        Variable<String> added = vars.put(var);
        Optional<Variable<String>> optionalVariable = vars.get(variableName);

        assertAll(() -> assertNotNull(added),
                () -> assertEquals(var, added),
                () -> assertTrue(optionalVariable.isPresent()));

        final double value = 0.293D;
        Variable<Double> otherVar = Variable.of(NUMBER, variableName, value);
        Variable<Double> editedVar = vars.edit(otherVar);
        Variable<?> editedFound = vars.get(variableName).get();

        assertAll(() -> assertEquals(otherVar, editedVar),
                () -> assertEquals(editedFound, editedVar),
                () -> assertNotEquals(added, editedFound),
                () -> assertEquals(editedFound.getValue(), value));
    }

    @Test
    @DisplayName("Should not edit variable that key not exists")
    void editVariableNotExistsTest() {
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        final double value = 0.293D;
        final String variableName = "name";
        Variable<Double> otherVar = Variable.of(NUMBER, variableName, value);

        assertThrows(IllegalStateException.class, () -> vars.edit(otherVar));
    }

    @Test
    @DisplayName("Should find all variables as a immutable map")
    void findAllVariablesTest() {
        final String variableName = "name";
        GlobalVariables vars = GlobalVariablesManager.getInstance();
        Variable<String> var = Variable.of(STRING, variableName, "EaseCI");

        Variable<String> added = vars.put(var);

        Map<String, Variable<?>> allVariables = vars.getAllVariables();

        assertAll(() -> assertTrue(allVariables.containsValue(added)),
                () -> assertTrue(allVariables.containsKey(added.getName())),
                () -> assertEquals(1, allVariables.size()));
    }
}
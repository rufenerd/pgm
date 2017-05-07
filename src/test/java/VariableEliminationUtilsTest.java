import static org.assertj.core.api.Java6Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class VariableEliminationUtilsTest {

  public static final double EPSILON = 1e-6;
  private DiscreteVariable x = new DiscreteVariable(2, "X");
  private DiscreteVariable y = new DiscreteVariable(2, "Y");
  private DiscreteVariable z = new DiscreteVariable(3, "Z");
  private DiscreteVariable a = new DiscreteVariable(4, "A"); //lazy 1-index
  private DiscreteVariable b = new DiscreteVariable(3, "B"); //lazy 1-index
  private DiscreteVariable c = new DiscreteVariable(3, "C"); //lazy 1-index
  private TableFactor xor;
  private TableFactor xPlusTenY;
  private TableFactor zMinusX;
  private TableFactor figureFourThreeA;
  private TableFactor figureFourThreeB;

  @Before
  public void setup() {
    final Set<Assignment> xAssignments = x.allAssignments();
    final Set<Assignment> yAssignments = y.allAssignments();
    final Set<Assignment> zAssignments = z.allAssignments();

    Map<Set<Assignment>, Double> xor = new HashMap<>();
    for (Assignment xAssignment : xAssignments) {
      for (Assignment yAssignment : yAssignments) {
        xor.put(ImmutableSet.of(xAssignment, yAssignment),
            (double) (xAssignment.getValue() ^ yAssignment.getValue()));
      }
    }
    this.xor = new TableFactor(xor);

    Map<Set<Assignment>, Double> xPlusTenY = new HashMap<>();
    for (Assignment xAssignment : xAssignments) {
      for (Assignment yAssignment : yAssignments) {
        xPlusTenY.put(ImmutableSet.of(xAssignment, yAssignment),
            (double) (xAssignment.getValue() + 10 * yAssignment.getValue()));
      }
    }
    this.xPlusTenY = new TableFactor(xPlusTenY);

    Map<Set<Assignment>, Double> zMinusX = new HashMap<>();
    for (Assignment xAssignment : xAssignments) {
      for (Assignment zAssignment : zAssignments) {
        zMinusX.put(ImmutableSet.of(xAssignment, zAssignment),
            (double) (zAssignment.getValue() - xAssignment.getValue()));
      }
    }
    this.zMinusX = new TableFactor(zMinusX);

    figureFourThreeA = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 1)), 0.5)
            .put(ImmutableSet.of(new Assignment(a, 1), new Assignment(b, 2)), 0.8)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 1)), 0.1)
            .put(ImmutableSet.of(new Assignment(a, 2), new Assignment(b, 2)), 0.0)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 1)), 0.3)
            .put(ImmutableSet.of(new Assignment(a, 3), new Assignment(b, 2)), 0.9)
            .build()
    );

    figureFourThreeB = new TableFactor(
        ImmutableMap.<Set<Assignment>, Double>builder()
            .put(ImmutableSet.of(new Assignment(b, 1), new Assignment(c, 1)), 0.5)
            .put(ImmutableSet.of(new Assignment(b, 1), new Assignment(c, 2)), 0.7)
            .put(ImmutableSet.of(new Assignment(b, 2), new Assignment(c, 1)), 0.1)
            .put(ImmutableSet.of(new Assignment(b, 2), new Assignment(c, 2)), 0.2)
            .build()
    );
  }

  @Test
  public void sumProductVariableElimination() throws Exception {
    final ImmutableSet<TableFactor> allFactors = ImmutableSet
        .of(xor, figureFourThreeA, figureFourThreeB, xPlusTenY, zMinusX);

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of()).scope).isEqualTo(ImmutableSet.of(a, b, c, x, y, z));

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of(z)).scope).isEqualTo(ImmutableSet.of(a, b, c, x, y));

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of(z, x)).scope).isEqualTo(ImmutableSet.of(a, b, c, y));

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of(b, z, x)).scope).isEqualTo(ImmutableSet.of(a, c, y));

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of(b, z, x)).scope).isEqualTo(ImmutableSet.of(a, y, c));

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of(b, z, x)).scope).isEqualTo(ImmutableSet.of(c, a, y));

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of(b, a, z, x)).scope).isEqualTo(ImmutableSet.of(c, y));

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of(b, a, y, z, x)).scope).isEqualTo(ImmutableSet.of(c));

    assertThat(VariableEliminationUtils.sumProductVariableElimination(
        allFactors,
        ImmutableList.of(b, a, y, z, x, c)).scope).isEqualTo(ImmutableSet.of());

    //TODO Test more than scope
  }

}
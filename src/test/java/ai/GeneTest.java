package ai;


import data.DatasetType;
import org.junit.Test;
import ui.State;

public class GeneTest {


    @Test
    public void geneticTest() {
        State state = State.getInstance();
        state.getDataset(DatasetType.BAYS29);
        PathGene gene = new PathGene();
        for (int i = 0; i <10; i++) {
            System.out.println(gene.initialize());
        }
    }
}

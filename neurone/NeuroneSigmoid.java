public class NeuroneSigmoid extends Neurone{

    public NeuroneSigmoid(int nbEntrees) {
        super(nbEntrees);
        //TODO Auto-generated constructor stub
    }

    @Override
    protected float activation(float valeur) {
        // TODO Auto-generated method stub
        return (float)(1 / (1 + Math.exp(-valeur)));
    }
    
}

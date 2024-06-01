public class NeuroneHeaviside extends Neurone
{
	// Fonction d'activation d'un neurone (peut facilement être modifiée par héritage)
	protected float activation(final float valeur) {return valeur >= 0 ? 1.f : 0.f;}
	
	// Constructeur
	public NeuroneHeaviside(final int nbEntrees) {super(nbEntrees);}
}

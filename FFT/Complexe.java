public interface Complexe
{
	public double reel();
	public double imag();
	public double mod();
	public double arg();

	// Renvoie la somme avec le paramètre, sans modifier l'objet
	public Complexe plus(Complexe complexe);
	// Renvoie la différence avec le paramètre, sans modifier l'objet
	public Complexe moins(Complexe complexe);
	// Renvoie le produit avec le paramètre, sans modifier l'objet
	public Complexe fois(Complexe complexe);
}

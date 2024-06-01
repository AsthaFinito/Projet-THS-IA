public class ComplexePolaire implements Complexe
{
	private double mod;
	private double arg;

	public ComplexePolaire(double mod, double arg)
	{
		this.mod = mod;
		this.arg = arg;
	}

	public double reel() {return mod*Math.cos(arg);}
	public double imag() {return mod*Math.sin(arg);}
	public double mod() {return mod;}
	public double arg() {return arg;}

	// Renvoie la somme avec le paramètre, sans modifier l'objet
	public Complexe plus(Complexe complexe)
	{
		return new ComplexeCartesien(reel(), imag()).plus(complexe);
	}

	// Renvoie la différence avec le paramètre, sans modifier l'objet
	public Complexe moins(Complexe complexe)
	{
		return new ComplexeCartesien(reel(), imag()).moins(complexe);
	}

	// Renvoie le produit avec le paramètre, sans modifier l'objet
	public Complexe fois(Complexe complexe)
	{
		return new ComplexePolaire(mod()*complexe.mod(), arg()+complexe.arg());
	}
}

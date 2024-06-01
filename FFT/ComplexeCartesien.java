public class ComplexeCartesien implements Complexe
{
	private double reel;
	private double imag;

	public ComplexeCartesien(double reel, double imag)
	{
		this.reel = reel;
		this.imag = imag;
	}

	public double reel() {return reel;}
	public double imag() {return imag;}
	public double mod() {return Math.sqrt(reel*reel+imag*imag);}
	public double arg() {return Math.atan2(imag, reel);}

	// Renvoie la somme avec le paramètre, sans modifier l'objet
	public Complexe plus(Complexe complexe)
	{
		return new ComplexeCartesien(reel()+complexe.reel(), imag()+complexe.imag());
	}

	// Renvoie la différence avec le paramètre, sans modifier l'objet
	public Complexe moins(Complexe complexe)
	{
		return new ComplexeCartesien(reel()-complexe.reel(), imag()-complexe.imag());
	}

	// Renvoie le produit avec le paramètre, sans modifier l'objet
	public Complexe fois(Complexe complexe)
	{
		final double rtemp = reel()*complexe.reel()-imag()*complexe.imag();
		imag = reel()*complexe.imag()+imag()*complexe.reel();
		return new ComplexeCartesien(rtemp, imag);
	}
}

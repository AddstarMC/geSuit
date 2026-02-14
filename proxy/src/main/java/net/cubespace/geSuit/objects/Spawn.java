package net.cubespace.geSuit.objects;

public class Spawn {
	private Location loc;
	private String name;
	
	public Spawn(String name, Location loc){
		this.name = name;
		this.loc = loc;
	}
	
	public void setLocation(Location loc){
		this.loc = loc;
	}
	
	public Location getLocation(){
		return loc;
	}
	
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	
	
}

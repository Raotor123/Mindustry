package io.anuke.mindustry.entities;

import static io.anuke.mindustry.Vars.*;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.effect.Fx;
import io.anuke.mindustry.input.Input;
import io.anuke.mindustry.resource.Mech;
import io.anuke.mindustry.resource.Recipe;
import io.anuke.mindustry.resource.Weapon;
import io.anuke.ucore.core.*;
import io.anuke.ucore.entities.DestructibleEntity;
import io.anuke.ucore.util.Angles;

public class Player extends DestructibleEntity{
	public Weapon weapon;
	public Mech mech = Mech.standard;
	public float breaktime = 0;
	
	public Recipe recipe;
	public int rotation;
	
	private Vector2 direction = new Vector2();
	private float speed = 1.1f;
	private float dashSpeed = 1.8f;
	
	public Player(){
		hitbox.setSize(5);
		hitboxTile.setSize(4f);
		
		maxhealth = 100;
		heal();
	}
	
	@Override
	public void damage(int amount){
		if(!Vars.debug)
			super.damage(amount);
	}
	
	@Override
	public void onDeath(){
		
		remove();
		Effects.effect(Fx.explosion, this);
		Effects.shake(4f, 5f, this);
		Effects.sound("die", this);
		
		Vars.control.setRespawnTime(respawnduration);
		ui.fadeRespawn(true);
	}
	
	@Override
	public void draw(){
		if(!Vars.showPlayer) return;
		
		if(Vars.snapCamera && Settings.getBool("smoothcam") && Settings.getBool("pixelate")){
			Draw.rect("mech-"+mech.name(), (int)x, (int)y, direction.angle()-90);
		}else{
			Draw.rect("mech-"+mech.name(), x, y, direction.angle()-90);
		}
		
	}
	
	@Override
	public void update(){
		
		float speed = this.speed;
		
		if(Inputs.keyDown("dash")){
			speed = dashSpeed;
			
			if(Vars.debug){
			//	speed *= 3f;
			}
		}
		
		if(health < maxhealth && Timers.get(this, "regen", 50))
			health ++;
		
		vector.set(0, 0);
		
		if(Inputs.keyDown("up"))
			vector.y += speed;
		if(Inputs.keyDown("down"))
			vector.y -= speed;
		if(Inputs.keyDown("left"))
			vector.x -= speed;
		if(Inputs.keyDown("right"))
			vector.x += speed;
		
		boolean shooting = !Inputs.keyDown("dash") && Inputs.buttonDown(Buttons.LEFT) && recipe == null && !ui.hasMouse() && !Input.onConfigurable();
		
		if(shooting && Timers.get(this, "reload", weapon.reload)){
			weapon.shoot(this);
			Sounds.play(weapon.shootsound);
		}
		
		if(Inputs.keyDown("dash") && Timers.get(this, "dashfx", 3) && vector.len() > 0){
			Angles.translation(direction.angle() + 180, 3f);
			Effects.effect(Fx.dashsmoke, x + Angles.x(), y + Angles.y());
		}
		
		vector.limit(speed);
		
		if(!Vars.noclip){
			move(vector.x*Timers.delta(), vector.y*Timers.delta());
		}else{
			x += vector.x*Timers.delta();
			y += vector.y*Timers.delta();
		}
		
		if(!shooting){
			direction.add(vector);
			direction.limit(speed*6);
		}else{
			float angle = Angles.mouseAngle(x, y);
			direction.lerp(vector.set(0, 1).setAngle(angle), 0.26f);
			if(MathUtils.isEqual(angle, direction.angle(), 0.05f)){
				direction.setAngle(angle);
			}
		}
	}
}

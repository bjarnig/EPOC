
BGenPulso : BGen
{ 	
	var paramValues, reverb, reverbBus;	
	
	*new { |id=0, description, duration, control, outBus=0, values|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, nil, nil).init(values);
	}
	
	init {|values|
		paramValues = values;
		reverbBus = Bus.audio(Server.local, 2);
		this.setDescription;
	}
	
	*loadSynthDefs {

	 	SynthDef(\pulso,
		{| out=0, density = 100, speed=10, freq=2, surface=0.5, entropy=10, amp, atk=0.01, sus=10, rel=0.01 |
		var trigger, dust, signal, env;
		env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,sus-atk-rel, rel]), doneAction:2);
		dust = Dust.kr(density, 0.5) * speed;
		trigger = dust + (SinOsc.kr(speed*0.5, mul:0.5, add:1) * LFNoise1.ar(density * 0.1, add:1, mul:entropy));
		signal = DynKlank.ar(`[[180, 90, 115, 172], nil, [0.1, 0.2, 0.3, 0.4]], Impulse.ar(trigger, mul:1.4-surface));
		signal = signal + (BrownNoise.ar(surface) * EnvGen.ar(Env.new([0, 1, 0],[0.0002, 0.04]), Impulse.ar(trigger)) * 0.8);
		signal = ((PitchShift.ar(signal, 0.02, freq, 0, 0.0001) * 0.5) + (signal * 0.5));
		signal = Pan2.ar(signal, 0) * 0.5;
		signal = signal * amp * env;
		Out.ar(out, signal);
	 	}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add; 
		
		SynthDef(\pulsoCombi, 
		{| Êd1 = 0.08, d2 = 0.09, d3 = 0.1, d4 = 0.15, d5 = 0.2,
Ê Ê Ê	t1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 50, f2 = 150, f3 = 250, f4 = 350, 
		f5 = 20000, in = 3, out = 0, amp=0.8, delayMult=0.1, decayMult=8.0, filtMult=1.0, mix=0.5|
Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê
Ê Ê Ê	var inB, outB, c1, c2, c3, c4, c5;
		inB = In.ar(in, 2);

	  	c1 = LPF.ar(CombC.ar(inB, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult);
Ê Ê Ê Ê	c2 = LPF.ar(CombC.ar(inB, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult);
Ê Ê Ê Ê	c3 = LPF.ar(CombC.ar(inB, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult);
Ê Ê Ê Ê	c4 = LPF.ar(CombC.ar(inB, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult);
Ê Ê Ê Ê	c5 = LPF.ar(CombC.ar(inB, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult);
Ê Ê Ê Ê
Ê Ê Ê Ê	outB = (((c1 + c2 + c3 + c4 + c5) * 0.4) * mix) + (inB * (1 - mix));
Ê Ê Ê Ê	Out.ar(out, outB * amp);

	}).add;
	}
	
	play {
	wrap = Bwrap.new(\pulso, paramValues);
	reverbBus = Bus.audio(Server.local, 2);
	wrap.set(\out, reverbBus);
	reverb = Bwrap.new(\pulsoCombi, [\in, reverbBus, \out, outBus]);
	this.update.value;
	reverb.play;	
	this.playDuration(duration);
	}
	
	playDuration {| length |
	
		if(length.notNil){
			Routine {
			1.do {
			wrap.play;
			length.wait;
			this.stop.value;
			}}.play;
		}{ 
			wrap.play;
		}
	}
	
	stop {
		
		wrap.stop;
		reverb.stop;
		reverbBus.free;
	}
	
	update {
	
	wrap.set(\density, control.density.linlin(0.0, 1.0, 0.001, 400));
	wrap.set(\speed, control.speed.linlin(0.0, 1.0, 0.001, 400));
	wrap.set(\freq, control.frequency.linlin(0.0, 1.0, 0.001, 2));
	wrap.set(\surface, control.surface);
	wrap.set(\entropy, control.entropy.linlin(0.0, 1.0, 0.001, 20));
	wrap.set(\amp, control.amplitude);
	wrap.set(\atk, control.attack);
	wrap.set(\sus, duration);
	wrap.set(\rel, control.release);
	reverb.set(\delayMult, (control.location));
	reverb.set(\mix, (control.color * 0.8));
	reverb.set(\decayMult, ((1 - control.color) * 0.5) + 0.05);
	
	}
	
	setDescription {
		description = "BGenRezim: 3 part resonant impulse oscillators. ";
	}
}

BGenRezim : BGen
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

	 	SynthDef(\rezim,
		{| out=0, freq1=80, freq2=90, freq3=100, speed=80, amp=0.5, atk=0.5, sus=1, rel=0.1, surface=0.0, color=0.5, entropy=0.0, filter=1000, density=0.5|
	 	var env, osca, oscb, oscc, oscd, signal, speedMod;
	 	env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], 1), doneAction:2);
	 	freq1= freq1 + (10 * LFNoise1.ar(freq:2, add:1,  mul:entropy));
	 	freq2= freq2 + (10 * LFNoise1.ar(freq:3, add:1,  mul:entropy));
	 	freq3= freq3 + (10 * LFNoise1.ar(freq:4, add:1,  mul:entropy));
	 	speedMod = ((speed * 2) * LFNoise1.ar(freq:8, add:1, mul:entropy));
		osca = DynKlank.ar(`[[freq1, freq1 * 2, freq1 * 3], nil, [0.5*color, 0.2*color, 0.2*color]], Impulse.ar(speed + speedMod));
	 	oscb = DynKlank.ar(`[[freq2, freq2 * 2, freq2 * 3], nil, [0.5*color, 0.2*color, 0.2*color]], Impulse.ar((speed * 2) + speedMod, mul:density));
	 	oscc = DynKlank.ar(`[[freq3, freq3 * 2, freq3 * 3], nil, [0.5*color, 0.2*color, 0.2*color]], Impulse.ar((speed * 4) + speedMod, mul:density));
		signal = osca + oscb + oscc;
		signal = signal * 0.1;
		signal = BPeakEQ.ar(signal, filter, 8, 0.1 + 10 * surface);
		signal = (signal * (1 - surface) + (signal.round(0.1) * (surface)));
		signal = LeakDC.ar(signal);
		signal = HPF.ar(signal, 70);
		signal = signal * env;
		signal = signal * amp;
		signal = Pan2.ar(signal, 0, 1);
		Out.ar(out, signal);
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add; 
		
		SynthDef(\rezimCombi, 
		{| Êd1 = 0.08, d2 = 0.09, d3 = 0.1, d4 = 0.15, d5 = 0.2,
Ê Ê Ê	t1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 50, f2 = 150, f3 = 250, f4 = 350, 
		f5 = 20000, in = 3, out = 0, amp=0.8, delayMult=0.1, decayMult=2.0, filtMult=1.0, mix=0.5|
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
	wrap = Bwrap.new(\rezim, paramValues);
	reverbBus = Bus.audio(Server.local, 2);
	wrap.set(\out, reverbBus);
	reverb = Bwrap.new(\rezimCombi, [\in, reverbBus, \out, outBus]);
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
	
	var newFrequency1, newFrequency2, newFrequency3, newFrequency4;
	var newAttack, newRelease, newSustain, newAmp, newColor, newSpeed;
	
	// Calculate params
	
	newAmp = control.amplitude.linlin(0.0, 1.0, 0.0, 2.0);
	newAttack = control.attack * duration;
	newRelease = control.release * duration;
	newSustain = duration - (newAttack + newRelease);	
	newColor = control.color.linlin(0.0, 1.0, 0.001, 3.0);
	newFrequency1 = 100 * control.frequency;
	newFrequency2 = 150 * control.frequency; 
	newFrequency3 = 200 * control.frequency; 
	newFrequency4 = 300 * control.frequency; 
	newSpeed = control.speed.linlin(0.0, 1.0, 0.001, 15.0);
	
	// Set params
	
	wrap.set(\freq1, newFrequency1);
	wrap.set(\freq2, newFrequency2);
	wrap.set(\freq3, newFrequency3);
	wrap.set(\atk, newAttack);
	wrap.set(\rel, newRelease);
	wrap.set(\sus, newSustain);
		
	wrap.set(\amp, newAmp);
	wrap.set(\speed, newSpeed);
	wrap.set(\color, newColor);
	wrap.set(\surface, control.surface);
	wrap.set(\entropy, control.entropy);
	wrap.set(\density, control.density);
	reverb.set(\mix, (control.location * 0.8));
	reverb.set(\decayMult, ((1 - control.location) * 0.5) + 0.05);
	wrap.set(\filter, 80 + (16000 * control.frequency));
	
	}
	
	setDescription {
		description = "BGenRezim: 3 part resonant impulse oscillators. ";
	}
}
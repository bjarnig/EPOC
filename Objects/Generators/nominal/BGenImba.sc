
BGenImba : BGen
{
	var paramValues, reverb, effectBus;

	*new { |id=0, description, duration, control, outBus=0, values|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, nil).init(values);
	}

	init {|values|
		paramValues = values;
		this.setDescription;
	}

	*loadSynthDefs {

	 	SynthDef(\imba,
		{| out=0, freq1=80, freq2=90, freq3=100, freq4=110, freq5=120, freq6=130, freq7=140, freq8=150, amp=0.5, atk=0.5, sus=1, rel=0.1, surface=0.0, harmAmp=0.0, speed=0.0|
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
	 	var osca = Impulse.ar(freq1);
		var oscb = Impulse.ar(freq2);
		var oscc = Impulse.ar(freq3);
		var oscd = Impulse.ar(freq4);
		var osce = Impulse.ar(freq1);
		var oscf = Impulse.ar(freq2);
		var oscg = Impulse.ar(freq3);
		var osch = Impulse.ar(freq4);
		var oscaHarm = Impulse.ar(freq1 * 2);
		var oscbHarm = Impulse.ar(freq2 * 2);
		var osccHarm = Impulse.ar(freq3 * 2);
		var oscdHarm = Impulse.ar(freq4 * 2);
		var osceHarm = Impulse.ar(freq1 * 2);
		var oscfHarm = Impulse.ar(freq2 * 2);
		var oscgHarm = Impulse.ar(freq3 * 2);
		var oschHarm = Impulse.ar(freq4 * 2);
		var signal = (osca + oscb + oscc + oscd + osce + oscf + oscg + osch + oscaHarm + oscbHarm + osccHarm + oscdHarm + osceHarm + oscfHarm + oscgHarm + oschHarm);

		// signal = (signal.clip * surface) + ((1 - surface) * signal);

		signal = (signal * surface) + ((1 - surface) * Formlet.ar(signal, freq1, 0.01, 0.1));
		signal = (signal * 0.8) + (signal * SinOsc.ar(speed) * 0.2);
		signal = LeakDC.ar(signal);
		signal = signal * env;
		signal = signal * 0.2 * amp;
		signal = Pan2.ar(signal, 0, 1);
		Out.ar(out, signal);
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add;

//		SynthDef(\imbaVerb, {|in=0, out=0, mix=0.01, room=0.5, damp=0.5|
//		var signal, reverb;
//		signal = In.ar(in, 2);
//		reverb = FreeVerb2.ar(signal[0], signal[1], mix, room, damp);
//		Out.ar(out, reverb);
//		}).add;

	SynthDef("imbaVerb",{| d1 = 0.2, d2 = 0.2, d3 = 0.2, d4 = 0.2, d5 = 0.2,
		t1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 50, f2 = 150, f3 = 250, f4 = 350,
		f5 = 20000, in = 3, out = 0, amp=0.8, delayMult=1.0, decayMult=1.0, filtMult=1.0, mix=0.5|

		var inB, outB, c1, c2, c3, c4, c5;
		inB = In.ar(in, 1);

		c1 = LPF.ar(CombC.ar(inB, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult);
		c2 = LPF.ar(CombC.ar(inB, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult);
		c3 = LPF.ar(CombC.ar(inB, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult);
		c4 = LPF.ar(CombC.ar(inB, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult);
		c5 = LPF.ar(CombC.ar(inB, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult);

		outB = (((c1 + c2 + c3 + c4 + c5) * 0.4) * mix) + (inB * (1 - mix));

		Out.ar([out, out + 1], outB * amp);

	}).add;
	}

	play {
	wrap = Bwrap.new(\imba, paramValues);
	effectBus = Bus.audio(Server.local, 2);
	wrap.set(\out, effectBus);
	reverb = Bwrap.new(\imbaVerb, [\in, effectBus, \out, outBus]);
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
		this.freeEffect;
	}

	update {

	var newFrequency1, newFrequency2, newFrequency3, newFrequency4, newFrequency5, newFrequency6, newFrequency7, newFrequency8;
	var newAttack, newRelease, newSustain, newAmp, newharmAmp, newSpeed;

	// Calculate params

	newAmp = control.amplitude.linlin(0.0, 1.0, 0.0, 2.0);
	newSpeed = control.speed.linexp(0.0, 1.0, 0.001, 25.0);
	newAttack = control.attack * duration;
	newRelease = control.release * duration;
	newSustain = duration - (newAttack + newRelease);
	newharmAmp = control.color;
	newFrequency1 = 50 * control.frequency;
	newFrequency2 = newFrequency1 + (control.density * 20 * (1 * (1 + (control.entropy.rand * 10))));
	newFrequency3 = newFrequency1 + (control.density * 20 * (2 * (1 + (control.entropy.rand * 10))));
	newFrequency4 = newFrequency1 + (control.density * 20 * (3 * (1 + (control.entropy.rand * 10))));
	newFrequency5 = newFrequency1 + (control.density * 20 * (4 * (1 + (control.entropy.rand * 10))));
	newFrequency6 = newFrequency1 + (control.density * 20 * (5 * (1 + (control.entropy.rand * 10))));
	newFrequency7 = newFrequency1 + (control.density * 20 * (6 * (1 + (control.entropy.rand * 10))));
	newFrequency8 = newFrequency1 + (control.density * 20 * (7 * (1 + (control.entropy.rand * 10))));

	// Set params

	wrap.set(\freq1, newFrequency1);
	wrap.set(\freq2, newFrequency2);
	wrap.set(\freq3, newFrequency3);
	wrap.set(\freq4, newFrequency4);
	wrap.set(\freq5, newFrequency5);
	wrap.set(\freq6, newFrequency6);
	wrap.set(\freq7, newFrequency7);
	wrap.set(\freq8, newFrequency8);
	wrap.set(\atk, newAttack);
	wrap.set(\rel, newRelease);
	wrap.set(\sus, newSustain);
	wrap.set(\amp, newAmp);
	wrap.set(\speed, newSpeed);
	wrap.set(\surface, control.surface);
	wrap.set(\harmAmp, newharmAmp);

	reverb.set(\mix, control.location);
	// reverb.set(\room, 0.5 + (control.location * 0.5));

	}

	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "BGenImba: 8 part additive impulse oscillators with 8 additional oscs for harmonics. ";
	}
}
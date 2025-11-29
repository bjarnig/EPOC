
BGenKari : BGen
{
	var paramValues, reverb, reverbBus, shaperBuf;

	*new { |id=0, description, duration, control, outBus=0, values|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, nil, nil).init(values);
	}

	init {|values|
		paramValues = values;
		this.setDescription;
	}

	*loadSynthDefs {

	 	SynthDef(\kari,
		{| out=0, freq1=80, freq2=90, freq3=100, freq4=110, freq5=120, freq6=130, freq7=140, freq8=150, amp=0.5, atk=0.5, sus=1, rel=0.1, surface=0.0, harmAmp=0.0, speed=0.0, entropy=0.0, shaperBuf=0|
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
	 	var freqMod = (SinOsc.ar(freq:(0.5*entropy), add:1,  mul:entropy));
	 	var osca = SinOsc.ar(freq1 * freqMod, mul:amp * 0.125 * amp);
		var oscb = SinOsc.ar(freq2 * freqMod, mul:amp * 0.125 * amp);
		var oscc = SinOsc.ar(freq3 * freqMod, mul:amp * 0.125 * amp);
		var oscd = SinOsc.ar(freq4 * freqMod, mul:amp * 0.125 * amp);
		var osce = SinOsc.ar(freq5 * freqMod, mul:amp * 0.125 * amp);
		var oscf = SinOsc.ar(freq6 * freqMod, mul:amp * 0.125 * amp);
		var oscg = SinOsc.ar(freq7 * freqMod, mul:amp * 0.125 * amp);
		var osch = SinOsc.ar(freq8 * freqMod, mul:amp * 0.125 * amp);
		var oscaHarm = SinOsc.ar(freq1 * 2, mul:amp * 0.125 * harmAmp);
		var oscbHarm = SinOsc.ar(freq2 * 2, mul:amp * 0.125 * harmAmp);
		var osccHarm = SinOsc.ar(freq3 * 2, mul:amp * 0.125 * harmAmp);
		var oscdHarm = SinOsc.ar(freq4 * 2, mul:amp * 0.125 * harmAmp);
		var osceHarm = SinOsc.ar(freq5 * 2, mul:amp * 0.125 * harmAmp);
		var oscfHarm = SinOsc.ar(freq6 * 2, mul:amp * 0.125 * harmAmp);
		var oscgHarm = SinOsc.ar(freq7 * 2, mul:amp * 0.125 * harmAmp);
		var oschHarm = SinOsc.ar(freq8 * 2, mul:amp * 0.125 * harmAmp);

		var signal = (osca + oscb + oscc + oscd + osce + oscf + oscg + osch + oscaHarm + oscbHarm + osccHarm + oscdHarm + osceHarm + oscfHarm + oscgHarm + oschHarm);
		signal = (Shaper.ar(shaperBuf, signal, 0.5) * surface) + ((1 - surface) * signal);
		signal = (signal * 0.75) + (signal * SinOsc.ar(0.01 + (5 * speed)) * 0.7);
		signal = LeakDC.ar(signal);
		signal = signal * env;
		signal = Pan2.ar(signal, 0, 1);
		Out.ar(out, signal);
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add;

		SynthDef(\kariVerb, {|in=0, out=0, mix=0.01, room=0.5, damp=0.5|
		var signal, reverb;
		signal = In.ar(in, 2);
		reverb = FreeVerb2.ar(signal[0], signal[1], mix, room, damp);
		Out.ar(out, reverb);
		}).add;
	}

	play {
	wrap = Bwrap.new(\kari, paramValues);
	shaperBuf = Buffer.alloc(Server.local, 512, 1, { |buf| buf.chebyMsg([1,1,1,0,1,1])});
	reverbBus = Bus.audio(Server.local, 2);
	wrap.set(\out, reverbBus);
	wrap.set(\shaperBuf, shaperBuf);
	reverb = Bwrap.new(\kariVerb, [\in, reverbBus, \out, outBus]);
	this.update.value;
	reverb.play;
	this.playDuration(duration);
	this.stop;
	}

	playDuration {| length |

		if(length.notNil){
			Routine {
			1.do {
			wrap.play;
			length.wait;
			}}.play;
		}{
			wrap.play;
		}
	}

	stop {
		wrap.stop;
		shaperBuf.free;
		reverb.stop;
		reverbBus.free;
	}

	update {

	var newFrequency1, newFrequency2, newFrequency3, newFrequency4, newFrequency5, newFrequency6, newFrequency7, newFrequency8;
	var newAttack, newRelease, newSustain, newharmAmp, shaperBuf;

	// Calculate params

	newAttack = control.attack * duration;
	newRelease = control.release * duration;
	newSustain = duration - (newAttack + newRelease);
	newharmAmp = control.color;
	newFrequency1 = 250 * control.frequency;

	if(newFrequency1 < 40, {newFrequency1 = 40});

	newFrequency2 = newFrequency1 + (control.density * 10 * 1);
	newFrequency3 = newFrequency1 + (control.density * 10 * 2);
	newFrequency4 = newFrequency1 + (control.density * 10 * 3);
	newFrequency5 = newFrequency1 + (control.density * 10 * 4);
	newFrequency6 = newFrequency1 + (control.density * 10 * 5);
	newFrequency7 = newFrequency1 + (control.density * 10 * 6);
	newFrequency8 = newFrequency1 + (control.density * 10 * 7);

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
	wrap.set(\amp, control.amplitude * 1.5);
	wrap.set(\speed, control.speed);
	wrap.set(\surface, control.surface);
	wrap.set(\entropy, control.entropy);
	wrap.set(\harmAmp, newharmAmp);
	reverb.set(\mix, control.location * 0.5);
	reverb.set(\room, 0.5 + (control.location * 0.5));

	}

	setDescription {
		description = "BSaddit: 8 part additive synthesis";
	}
}
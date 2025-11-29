
BGenSaga : BGen
{
	var paramValues, <>sound, reverb, buf, reverbBus;

	*new { |id=0, description, duration, control, outBus=0, values, sound, buf|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, nil, nil, nil).init(values, sound);
	}

	init {|values, snd|
		paramValues = values;
		sound = snd;
		this.setDescription;
	}

	*loadSynthDefs {

	 	SynthDef(\sagaGrain,
		{|buf, outBus, rate=1, trate=12, grDur=0.5, pos=0.1, atk=4, sus=5, rel=6, amp=1, lopass=19000, hipass=80, entropy=0.0|
		var dur, clk, pan, signal, env, filtEnv;
		dur = 4 * grDur;
		clk = Impulse.kr(trate);
		pos = (pos * BufDur.kr(buf)) + TRand.kr(0, 0.01, clk) + WhiteNoise.ar(mul:entropy * 0.5, add:1);
		pan = WhiteNoise.kr(0.6);
		env = EnvGen.ar(Env.new([0,1,1,0],[atk,sus,rel]), doneAction:2);
		filtEnv = EnvGen.ar(Env.new([0,1,1,0],[atk/4,sus,rel/1.2], [-8]), doneAction:2);
		signal = TGrains.ar(2, clk, buf, rate, pos, dur, pan, 0.1) * env * amp;
		signal = LeakDC.ar(signal);
		signal = HPF.ar(signal, hipass);
		signal = LPF.ar(signal, lopass + ((20000 - lopass) * filtEnv));
		Out.ar(outBus, signal);}
		).add;

		SynthDef(\sagaVerb, {|in=0, out=0, mix=0.01, room=0.5, damp=0.8|
		var signal, reverb;
		signal = In.ar(in, 2);
		reverb = FreeVerb2.ar(signal[0], signal[1], mix, room, damp);
		Out.ar(out, reverb);
		}).add;
	}

	play {
	wrap = Bwrap.new(\sagaGrain, paramValues);
	buf = Buffer.read(Server.local, sound);
	reverbBus = Bus.audio(Server.local, 2);
	wrap.set(\buf, buf);
	wrap.set(\outBus, reverbBus);
	reverb = Bwrap.new(\sagaVerb, [\in, reverbBus, \out, outBus]);
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
			buf.free;
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

	var pos, rate, trate, atk, sus, rel, amp, lopass, hipass, grDur;

	// Calculate params

	pos = control.location;
	rate = control.frequency + 0.5;
	trate = control.speed.linlin(0,1,0.01,20);
	atk = control.attack * duration;
	rel = control.release * duration;
	sus = duration - (atk + rel);
	amp = control.amplitude * 4;
	lopass = 19990 * (1.01 - control.surface);
	hipass = 400 * (1.01 - control.surface);
	grDur = control.density;

	if(lopass > 19999, {lopass = 19999});
	if(hipass < 25, {hipass = 25});

	// Set params

//	("pos: " ++ pos).postln;
//	("rate: " ++ rate).postln;
//	("trate: " ++ trate).postln;
//	("atk: " ++ atk).postln;
//	("sus: " ++ sus).postln;
//	("rel: " ++ rel).postln;
//	("amp: " ++ amp).postln;
//	("lopass: " ++ lopass).postln;
//	("hipass: " ++ hipass).postln;

	wrap.set(\pos, pos);
	wrap.set(\rate, rate);
	wrap.set(\trate, trate);
	wrap.set(\grDur, grDur);
	wrap.set(\atk, atk);
	wrap.set(\sus, sus);
	wrap.set(\rel, rel);
	wrap.set(\amp, amp);
	wrap.set(\entropy, control.entropy);
	wrap.set(\lopass, lopass);
	wrap.set(\hipass, hipass);

	reverb.set(\mix, control.color);
	reverb.set(\room, 0.35 + (control.color * 0.5));

	}

	setDescription {
		description = "A granular static position player using TGrains. Density and speed control similar granular parameters. Color a seperate reverb, location for loction in sample, surface for distortion and filtering.
		";
	}
}
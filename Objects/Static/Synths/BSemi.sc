
BSemi : Bsynth
{
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}

	init {|values|
		wrap = Bwrap.new(\emi, values);
		this.setDescription;
	}

	*loadSynthDefs {

	SynthDef(\emi, {arg outBus=0, amp=0.8, buf=1, atk=15, sus=8, rel=12, rate=60, grAtk=0.6, grRel=0.1,
	sinFreq=66, sinAmp=0.8, noiseFreq=3000, noiseAmp=1.2, impulseFreq=6000, startPos=0.1,
	impulseAmp=0.1, sawFreq=80, sawAmp=2, sampFreq=2300, sampAmp=0.7, hipass=40;
	var signal;
	var trigger= (Impulse.ar(rate) * 0.9) + (Dust.ar(rate) * 0.1);
	var env= EnvGen.ar(Env.perc(grAtk, grRel, 1), trigger, 1, 0, (1/rate));
	var bassEnv= EnvGen.ar(Env.new([0,1,1,0],[atk,sus,rel], -2), doneAction:2);
	var trebleEnv= EnvGen.ar(Env.new([0,1,1,0],[atk/2,sus,rel/2], -2));
	var impulse = BPF.ar(Impulse.ar(impulseFreq,env), impulseFreq + (impulseFreq/4), 0.1) * impulseAmp * env;
	var saw = LPF.ar(Saw.ar(sawFreq,env),sawFreq+(sawFreq/4),0.1) * sawAmp;
	var sine = LPF.ar(SinOsc.ar(sinFreq,mul:env), sinFreq + (sinFreq/3),0.3) * sinAmp;
	var noise = BPF.ar(PinkNoise.ar(mul:env), noiseFreq, 0.1) * noiseAmp;
	var sample = BPF.ar(PlayBuf.ar(1, buf, BufRateScale.kr(buf), loop: 0, trigger:trigger, startPos:startPos),sampFreq, 0.4) * sampAmp;
	var bass = (sine + saw);
	var treble = (impulse + noise + sample);
	bass = bass * bassEnv;
	treble = treble * trebleEnv;
	signal = LeakDC.ar(bass + treble);
	signal = signal * amp;
	signal = HPF.ar(signal, hipass);
	Out.ar(outBus, Pan2.ar(signal, 0));
	}).add;
	}

	play {| length |

		if(length.notNil){
			Routine {
			1.do {
			wrap.play;
			length.wait;
			wrap.synth.free;
			}}.play;
		}{
			wrap.play;
		}
	}

		setDescription {
		description = "BSemi: Synthesis + sample stream player.";
	}
}
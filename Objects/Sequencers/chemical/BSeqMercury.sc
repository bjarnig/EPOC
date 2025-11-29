
BSeqMercury : BSeq
{
	var <>durations, <>amplitudes, <>pitches, currentPattern, reverb, effectBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyRate, proxyPan, proxyGrAmp, proxyDistort,
	proxyLpf, <>isPlaying, preControl;

	*new { |id=0, description="BSeqMercury", duration=10, control, outBus=0, durations, amplitudes, pitches, load=1|

		^super.newCopyArgs(id, description, duration, control, outBus, durations, amplitudes, pitches).init(load);
	}

	init {|load=1|

		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new;

		if(load > 0, {
		if(durations.isNil, {this.durations = [0.5, 0.5]});
		if(this.amplitudes.isNil, {this.amplitudes = [1, 1]});
		if(this.pitches.isNil, {this.pitches = [1, 1]});
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\mercuryReso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play; });
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \durations, {durations = paramValue});
	if(paramName == \amplitudes, {amplitudes = paramValue});
	if(paramName == \pitches, {pitches = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	*loadSynthDefs {

		SynthDef(\mercury,
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp, atk=0.01, rel=0.01, freq=1, lop=20000, hip=20, distort=0.0, lpf=1.0|
		var env = EnvGen.ar(Env.new([0, 1, 0.2, 0],[0.4 * (grdur*0.3), grdur, grdur * 0.5]), doneAction:2);
		var osc2;
		var signal = FSinOsc.ar(freq, mul:0.2) + FSinOsc.ar(freq*0.98, mul:0.2) + FSinOsc.ar(FSinOsc.ar(freq*0.055, mul:0.2), mul:0.2) + FSinOsc.ar(FSinOsc.ar(freq*0.0015, mul:0.2);, mul:0.2);
		signal = RLPF.ar(signal, 20000 * lpf, 1);
		signal = (Impulse.ar(freq:freq, mul:0.75).distort * env * distort) + (signal * (1.0-distort));
		signal = HPF.ar(signal, 60);
		signal = signal * env * grAmp * amp;
		signal = LPF.ar(signal, lop) + HPF.ar(signal, hip);
		osc2 = HPF.ar(LPF.ar(PinkNoise.ar(0.008), freq), 100);
		osc2 = osc2 + RLPF.ar(Pulse.ar([freq,freq*0.925],0.5,0.1), freq, 0.05);
		signal = Pan2.ar(signal, pan) + Pan2.ar(osc2[0], pan) + Pan2.ar(osc2[1], pan);
		signal = signal * env * grAmp * amp;
		Out.ar(bus, signal);
		}).add;

		SynthDef(\mercuryReso, {arg inBus=0,
							outBus=0,
							dryWet,
							maxDelay=8,
							delay=0.9,
							feedback=0.35;
		Ê Êvar fx, sig, output;
		Ê Êsig = InFeedback.ar(inBus,2);
		Ê Êfx = sig + LocalIn.ar(2);
		Ê Êfx = DelayC.ar(fx, maxDelay, delay);
		Ê ÊLocalOut.ar(fx * feedback);
		   output = (fx * dryWet) + (sig * (1-dryWet));
Ê Ê		   Out.ar(outBus, output);

		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1]).add;
	}

	update {

		var pat, dur, grDur, atk, sus, rel, grAtk, grRel, amp, pan, reverb, pitchWrap, rate;
		var decayMin, decayMax, ratePattern, panPattern, newDistort, newLpf, n=0, entropy;

		grDur = 0.001 + (0.1 * control.density);
		grAtk = (control.surface / 4) * grDur;
		grRel = grAtk;
		amp = Pseq(BUtils.limitArrayByPosition(control.position, amplitudes) * Env.new([0.0, 0.2, 0.5, 0.75, 8],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude), inf);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		pan = control.location;
		panPattern = Pbrown(Env.new([-0.9, -0.6, -0.1, 0.3, 0.6],[0.25, 0.25, 0.25, 0.25]).at(control.location), Env.new([-0.6, -0.1, 0.1, 0.6, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.location), 0.1);
		pitchWrap = Env.new([60, 100, 150, 10000, 19000], [0.25, 0.25, 0.25, 0.25]);
		ratePattern = Pseq(BUtils.limitArrayByPosition(control.position, pitches) * pitchWrap.at(control.frequency), inf);
		dur = durations * Env.new([8, 2, 1, 0.25, 0.01],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		entropy = Pseq(BUtils.limitArrayByPosition(control.position, dur), inf) * Pwhite(rrand(1 - control.entropy, 1), 1.0);

		if(control.surface < 0.5, {newLpf = (control.surface + 0.01); newDistort=0}, {newLpf = 1.0; newDistort=control.surface - 0.5});
		if(control.surface == 0.5, {newDistort=0; newLpf=1 });

		this.rev;

	   	if(this.isPlaying > 0,
		{
			if(control.surface != preControl.surface, {proxyDistort.source = newDistort});
			if(control.surface != preControl.surface, {proxyLpf.source = newLpf});
			if(control.surface != preControl.surface, {proxyAtk.source = grAtk});
			if(control.surface != preControl.surface, {proxyRel.source = grRel});
			if(control.entropy != preControl.entropy || control.speed != preControl.speed || control.position != preControl.position, {proxyDur.source = entropy});
			if(control.density != preControl.density, {proxyGrDur.source = grDur});
			if(control.frequency != preControl.frequency || control.position != preControl.position, {proxyRate.source = ratePattern});
			if(control.location != preControl.location, {proxyPan.source = panPattern});
			if(control.amplitude != preControl.amplitude  || control.position != preControl.position, {proxyGrAmp.source = amp});
		},
		{
			('Mercury start'.postln);
			proxyDistort = PatternProxy(newDistort);
			proxyLpf = PatternProxy(newLpf);
			proxyDur = PatternProxy(entropy);
			proxyGrDur = PatternProxy(grDur);
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(ratePattern);
			proxyPan = PatternProxy(panPattern);
			proxyGrAmp = PatternProxy(amp);

			currentPattern.stop;

			pat = Pbind(
			\instrument, 'mercury',
			\dur, proxyDur,
			\grdur, proxyGrDur,
			\atk, proxyAtk,
			\rel, proxyRel,
			\freq, proxyRate,
			\pan, proxyPan,
			\grAmp, proxyGrAmp,
			\distort, proxyDistort,
			\lpf, proxyLpf,
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')),
			\bus, effectBus
		);

		this.playDuration(pat);
		this.isPlaying = 1;

		});

		preControl.copy(this.control);
	}

	rev {

		if(control.color != preControl.color, {reverb.set(\dryWet, control.color)});
		if(control.density != preControl.density, {reverb.set(\feedback, Env.new([0.05, 0.1, 0.5, 0.75, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.density))});
		if(control.speed != preControl.speed, {reverb.set(\delay, Env.new([0.025, 0.05, 0.075, 0.1, 0.15],[0.25, 0.25, 0.25, 0.25]).at(control.speed))});
	}

	play {

		this.update.value;
	}

	playDuration
	{|pat|

		Routine
		{
			1.do({
		     currentPattern = pat.play;
			duration.wait;
			currentPattern.stop;
			this.isPlaying = 0;
		})
		}.play;
	}

	stop {arg release=0;

		Routine
		{
			1.do({

			if(release > 0, {

			proxyGrAmp.source = Pif(Ptime(inf) <= (release), Env.new([control.amplitude, 0],[release], [-2]));

			});

			release.wait;
			currentPattern.stop;
			this.isPlaying = 0;
		})
		}.play;
	}

	dispose {

		this.freeEffect;
	}

	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "";
	}
}

BSeqStarch : BSeq
{
	var <>durations, <>soundsA, <>soundsB, interpolDur, buffersA, buffersB, currentPattern, reverb, effectBus, distortion, distBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyRate, proxyPan, proxyGrAmp, proxyBuf, proxyDist, proxyInterpolate, <>isPlaying, preControl;
	
	*new { |id=0, description="BSeqStarch", duration=10, control, outBus=0, durations, soundsA, soundsB, interpolDur=10, pathA, pathB, load=1|
		
		^super.newCopyArgs(id, description, duration, control, outBus, durations, soundsA, soundsB, interpolDur).init(load); 
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new; 
		
		if(load > 0, {
		
		if(this.durations.isNil, {this.durations = [0.5, 0.5]});
		buffersA = this.readFiles(soundsA).value;
		buffersB = this.readFiles(soundsB).value;
		
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\starchReso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
		
		distBus = Bus.audio(Server.local, 2);
		distortion = Bwrap.new(\starchCrusher, [\inBus, distBus, \outBus, effectBus]);
		distortion.play; });
	}
	
	setParam {|paramName, paramValue| 
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \durations, {durations = paramValue; });
	if(paramName == \soundsA, {soundsA = paramValue; });
	if(paramName == \soundsB, {soundsB = paramValue; });
	if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	readFiles {arg path; 
		var files, buffers;
		if(path.isNil, {path = BConstants.monoImpulses});
		files = SoundFile.collect(path);
		buffers = files.collect { |sf| var b;
		b = Buffer.read(Server.local, sf.path);};
		buffers;
 		^buffers;
	}
	
	*loadSynthDefs {
		
		SynthDef(\starch, 
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		var signal = PlayBuf.ar(1, buf, rate * BufRateScale.ir(buf), 1, offset * BufFrames.ir(buf), 0);
	
		signal = signal * env * grAmp;
		signal =  Compander.ar(signal * 2, signal, 1, 1, 0.05, 0.01, 0.01);
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add;  	
		
		SynthDef(\starchCrusher, {| inBus=0, outBus=0, samplerate=44100, bitsize=16, fxlevel=0.5, level=0.5| 
		Ê Êvar fx, sig, bitRedux; 
		Ê Êsig = InFeedback.ar(inBus, 2);
		   bitRedux = (Clipper8.ar(sig) * 0.3) +  (Clipper8.ar(HPF.ar(sig, 4000), -0.4, 0.4) * 0.8); // + OSWrap8.ar(sig, 0.2, 0.8);
		Ê ÊOut.ar(outBus, (bitRedux*fxlevel) + (sig * level))
		}).add; 
		
		SynthDef(\starchReso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, predelay=0.048,combdecay=4, allpassdecay=14|
		var sig, y, z;
		sig = InFeedback.ar(inBus, 2); 
		z = DelayN.ar(sig, 0.1, predelay);
		y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.05, rrand(0.03, 0.05), combdecay).distort })); 
		6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
		// sig = BBandStop.ar(sig, 2000, 0);
		Out.ar(outBus, ((sig * (1 - dryWet)) + (y * (dryWet*0.5))) * amp);
		}).add;
	}
	
	update {
		
		var pat, dur, grDur, readFrom, readTo, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1, freqMin, freqMax;
		var decayMin, decayMax, offsetPattern, ratePattern, panPattern, durs, sndSel;
		var sndIndexLowerA, sndIndexUpperA, sndIndexLowerB, sndIndexUpperB, interpolate, invEntropy, invEntropy2;
		
		if(control.position < 0.01, {control.position = 0.01});
				
		grDur = Env.new([0.05, 0.3, 1.5, 4, 8],[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grAtk = (control.surface / 2) * grDur;
		grRel = grAtk;
		amp = Env.new([0.0, 0.2, 0.5, 0.75, 8],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		
		dur = durations * Env.new([8.1, 2.1, 1.0, 0.25, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.speed); 
		rate = Env.new([0.1, 0.5, 1, 2, 8],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		pan = control.location.linlin(0, 1, -1, 1);
		ratePattern = Pwhite(0.99 * rate, rate * 1.01);
		
		if(pan > 0.5, {panPattern = Pwhite(pan, (pan * 1.4).min(1.0))}, {panPattern = Pwhite((pan * 0.6).max(-1.0), pan)});
		
		sndIndexUpperA = buffersA.size * control.density; 
		sndIndexLowerA = sndIndexUpperA * (1 - control.entropy);
		
		sndIndexUpperB = buffersB.size * control.density; 
		sndIndexLowerB = sndIndexUpperB * (1 - control.entropy);
		
		durs = Pseq(dur, inf) * Pfunc({rrand(1 - control.entropy, 1)}); // Pwhite(rrand(1 - control.entropy, 1), 1.0);
		sndSel = Pfunc { |e| if(e.interpolate > 0.5, {buffersA.at(((buffersA.size-1) * control.density).rand).bufnum},{buffersB.at(((buffersB.size-1) * control.density).rand).bufnum})};
		interpolate = Pseg( Pseq([0.001, 0.99],inf), Pseq([interpolDur*control.position, interpolDur*control.position],inf), \linear);
		this.rev;
	   	
	   	if(this.isPlaying > 0,
		{			
			if(control.density != preControl.density, {proxyGrDur.source = grDur; proxyBuf.source = sndSel});
			if(control.surface != preControl.surface, {proxyAtk.source = grAtk});
			if(control.surface != preControl.surface, {proxyRel.source = grRel});
			if(control.frequency != preControl.frequency, {proxyRate.source = ratePattern});
			if(control.location != preControl.location, {proxyPan.source = panPattern});
			if(control.amplitude != preControl.amplitude, {proxyGrAmp.source = amp});
			if(control.speed != preControl.speed, {proxyDur.source = durs});
			if(control.entropy != preControl.entropy, {proxyBuf.source = sndSel; proxyDur.source = durs});
			if(control.position != preControl.position, {proxyInterpolate.source = interpolate});
		}, 
		{
			('Starch start'.postln);
			proxyDur = PatternProxy(durs);
			proxyGrDur = PatternProxy(grDur); 
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(ratePattern);
			proxyPan = PatternProxy(panPattern);
			proxyGrAmp = PatternProxy(amp);
			proxyBuf = PatternProxy(sndSel);
			proxyInterpolate = PatternProxy(interpolate);
			
			currentPattern.stop;
			pat = Pbind(
			\instrument, 'starch',
			\interpolate, proxyInterpolate,
			\dur, proxyDur,
			\grdur, proxyGrDur,
			\atk, 0,
			\rel, proxyRel,
			\buf, sndSel,
			\offset, 0,
			\rate, proxyRate,
			\pan, proxyPan, 
			\grAmp, proxyGrAmp,
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')),
			\bus, distBus;
		);
		
		this.playDuration(pat);
		this.isPlaying = 1;
		
		});
		
		preControl.copy(this.control);
	}
	
	rev {
		
		if(control.color != preControl.color, {reverb.set(\dryWet, Env.new([0.0, 0.02, 0.2, 0.4, 0.6],[0.25, 0.25, 0.25, 0.25]).at(control.color))}); 
		if(control.surface != preControl.surface, { 
		distortion.set(\fxlevel, Env.new([0.0, 0.02, 0.2, 0.65, 0.8],[0.25, 0.25, 0.25, 0.25]).at(control.surface)); 
		distortion.set(\level, Env.new([1.0, 0.98, 0.6, 0.35, 0.2],[0.25, 0.25, 0.25, 0.25]).at(control.surface)) 
		
		});
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
		buffersA.do({arg item; item.free;});
		buffersB.do({arg item; item.free;});
	}
	
	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free}); 
		 if(distBus.index.isNil.not, {distBus.free}); 
		 if(distortion.synth.isPlaying, {distortion.stop});
	}
	
	setDescription {
		description = "BSeqStarch";
	}
}
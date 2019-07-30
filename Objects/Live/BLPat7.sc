
BLPat7 : BSeq
{
	var <>sounds, <>freqSeq, <>deltaSeq, <>durSeq, <>soundSeq, <>ampSeq, buffers, currentPattern, reverb, effectBus, distortion, distBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyRate, proxyPan, proxyGrAmp, proxyBuf, proxyDist, proxyDelta, proxyPosition, <>isPlaying, preControl;
	
	*new { |id=0, description="BLPat7", duration=10, control, outBus=0, sounds, freqSeq, deltaSeq, durSeq, soundSeq, ampSeq, load=1|
		
		^super.newCopyArgs(id, description, duration, control, outBus, sounds, freqSeq, deltaSeq, durSeq, soundSeq, ampSeq).init(load); 
	}
	
	init {|load=1| 
		
		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new; 		
		
		if(load > 0, {
		
		if(buffers.isNil, {buffers = this.readFiles.value}); 
		if(freqSeq.isNil, {this.freqSeq = [1.0, 1.0]});
		if(deltaSeq.isNil, {this.deltaSeq = [0.5, 0.5]});
		if(durSeq.isNil, {this.durSeq = [0.5, 0.5]});
		if(soundSeq.isNil, {this.soundSeq = [0, 0]});
		if(ampSeq.isNil, {this.ampSeq = [1.0, 1.0]});
		
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLPat7Reso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
		
		distBus = Bus.audio(Server.local, 2);
		distortion = Bwrap.new(\bLPat7Crusher, [\inBus, distBus, \outBus, effectBus]);
		distortion.play;
		
		});
	}
	
	setParam {|paramName, paramValue| 
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \sounds, {sounds = paramValue});
	if(paramName == \freqSeq, {freqSeq = paramValue});
	if(paramName == \deltaSeq, {deltaSeq = paramValue});
	if(paramName == \durSeq, {durSeq = paramValue});
	if(paramName == \soundSeq, {soundSeq = paramValue});
	if(paramName == \ampSeq, {ampSeq = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	readFiles {  
		var files, buffers, path;
		if(this.sounds.isNil, {path = BConstants.stereoImpulses}, {path = this.sounds});
		files = SoundFile.collect(path);
		buffers = files.collect { |sf| var b;
		b = Buffer.read(Server.local, sf.path);};
		buffers;
 		^buffers;
	}
	
	*loadSynthDefs {
		
		SynthDef(\bLPat7, 
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate * BufRateScale.ir(buf), 1, offset * BufFrames.ir(buf), 0);
		signal = signal * env * grAmp;
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add; 
		
		SynthDef(\bLPat7Crusher, {| inBus=0, outBus=0, samplerate=44100, bitsize=16, fxlevel=0.5, level=0.5, filter=1.0, filtamp=1.0| 
		Ê Êvar fx, sig, downsamp, bitRedux; 
		Ê Êsig = InFeedback.ar(inBus, 2);
		   sig = BPeakEQ.ar(sig, 1000, 0.8, filter, filtamp); 
		   downsamp = Latch.ar(sig, Impulse.ar(samplerate*0.5));
		   bitRedux = downsamp.round(0.5 ** bitsize);
		Ê ÊOut.ar(outBus, (bitRedux*fxlevel) + (sig * level))
		}).add; 	
		
		SynthDef(\bLPat7Reso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, roomsize=240, revtime=4.85, damping=0.21, inputbw=0.19, 
		earlylevel=(-12), taillevel=(-11), spread = 15|
		var freqs, ringtimes, input, signal, signal2, output; 
		freqs = Control.names([\freqs]).kr(Array.rand(128, 60.0, 200));
		ringtimes = Control.names([\ringtimes]).kr(Array.rand(128, 0.5, 8));
		input = In.ar(inBus, 2); 
		signal = DynKlank.ar(`[ 
		freqs,		
		Array.rand(128, 0.4, 0.7),
		ringtimes 			
		], input); 
		
		signal2 = (GVerb.ar( 
		input,
		roomsize, 
		revtime, 
		damping, 
		inputbw, 
		spread, 
		0,
		earlylevel.dbamp, 
		taillevel.dbamp,
		roomsize, dryWet) * 1.3) + (input*(1-dryWet));
		
		signal = signal * 0.001; 
		signal2 = signal2 * 0.6;
		signal = (signal + signal2) * dryWet;
		output = signal + (input * (1 - dryWet));
		output = output * amp;
		Out.ar(outBus, output);
		}).add;
	}
	
	update {
		
		var pat, grDur, readFrom, readTo, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1, freqMin, freqMax;
		var decayMin, decayMax, offsetPattern, durs, sndSel, delta;
		
		grDur = 4 * control.density;
		grAtk = (control.surface / 2) * grDur;
		grRel = grAtk;
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		
 		rate = Pseq(BUtils.limitArrayByPosition(control.position, freqSeq) * Env.new([0.1, 0.5, 1, 2, 8],[0.25, 0.25, 0.25, 0.25]).at(control.frequency), inf);
		durs = Pseq(BUtils.limitArrayByPosition(control.position, durSeq) * Env.new([8, 2, 1, 0.25, 0.05],[0.25, 0.25, 0.25, 0.25]).at(control.density), inf) * Pwhite(1 - control.entropy, 1); 
		// sndSel = Pseq(BUtils.limitArrayByPosition(control.position, soundSeq) + buffers[0].bufnum, inf);
		sndSel = Prand(buffers, inf); 
		delta = Pseq(BUtils.limitArrayByPosition(control.position, deltaSeq) * Env.new([8, 2, 1, 0.25, 0.05],[0.25, 0.25, 0.25, 0.25]).at(control.speed), inf) * Pwhite(1 - control.entropy, 1);
		amp = Pseq(BUtils.limitArrayByPosition(control.position, ampSeq) * Env.new([0.0, 0.2, 0.5, 0.75, 8],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude), inf) * Pwhite(1 - control.entropy, 1);
		pan = Pwhite(Env.new([-1.0, -0.8, -0.1, 0.3, 0.6],[0.25, 0.25, 0.25, 0.25]).at(control.location), Env.new([-0.6, -0.1, 0.1, 0.8, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.location)); 
		
		this.rev;
	   	
	   	if(this.isPlaying > 0,
		{			
			if(control.density != preControl.density || control.position != preControl.position, {proxyGrDur.source = grDur; proxyBuf.source = sndSel});
			if(control.surface != preControl.surface, {proxyAtk.source = grAtk});
			if(control.surface != preControl.surface, {proxyRel.source = grRel});
			if(control.frequency != preControl.frequency  || control.position != preControl.position, {proxyRate.source = rate});
			if(control.location != preControl.location, {proxyPan.source = pan});
			if(control.amplitude != preControl.amplitude  || control.position != preControl.position, {proxyGrAmp.source = amp});
			if(control.speed != preControl.speed  || control.position != preControl.position, {proxyDur.source = durs; proxyDelta.source = delta});
			if(control.entropy != preControl.entropy, {proxyBuf.source = sndSel; proxyDelta.source = delta; proxyDur.source = durs; proxyGrAmp.source = amp });
		}, 
		{
			('BLPat7 start'.postln);
			proxyDur = PatternProxy(durs);
			proxyGrDur = PatternProxy(grDur); 
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(rate);
			proxyPan = PatternProxy(pan);
			proxyGrAmp = PatternProxy(amp);
			proxyBuf = PatternProxy(sndSel); 
			proxyDelta = PatternProxy(delta); 

			currentPattern.stop;
			pat = Pbind(
			\instrument, 'bLPat7',
			\dur, proxyDur,
			\delta, proxyDelta, 
			\grdur, proxyGrDur,
			\atk, 0,
			\rel, proxyRel,
			\buf, proxyBuf,
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
		
		if(control.color != preControl.color, {reverb.set(\dryWet, control.color)}); 
		if(control.surface != preControl.surface, { 
		distortion.set(\samplerate, Env.new([44100, 44100, 44100, 20000, 8000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		distortion.set(\bitsize, Env.new([16, 16, 16, 12, 10],[0.25, 0.25, 0.25, 0.25]).at(control.surface)); 
		distortion.set(\fxlevel, Env.new([0.2, 0.4, 0.5, 0.85, 1.1],[0.25, 0.25, 0.25, 0.25]).at(control.surface)); 
		distortion.set(\level, Env.new([0.8, 0.6, 0.5, 0.25, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.surface)); 
		distortion.set(\filter, Env.new([-12, -6, 0.0, 0.0, 0.0],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		distortion.set(\filtamp, Env.new([2.5, 1.5, 1.0, 1.0, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.surface))
		
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
		if(this.isPlaying == 1, {this.stop.value});
		this.freeEffect;
		this.isPlaying = 0;
		buffers.do({arg item; item.free;});
		buffers = nil;
	}
	
	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free}); 
		 if(distBus.index.isNil.not, {distBus.free}); 
		 if(distortion.synth.isPlaying, {distortion.stop});
	}
	
	setDescription {
		description = "BLPat7";
	} 
}
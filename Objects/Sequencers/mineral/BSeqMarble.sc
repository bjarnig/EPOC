BSeqMarble : BSeq
{
	var <>durations, <>amplitudes, <>amplitudesDuration, <>curveType, currentPattern, reverb, effectBus;
	var proxyDur, proxyGrDur, proxyAtk, proxySus, proxyRel, proxyRate, proxyPan, proxyAmp, proxyGrAmp, proxySurface; 
	var <>isPlaying, buf, preControl;
	
	*new { |id=0, description="BSeqMarble", duration=10, control, outBus=0, durations, amplitudes, amplitudesDuration, curveType=0, load=1|
		
		^super.newCopyArgs(id, description, duration, control, outBus, durations, amplitudes, amplitudesDuration, curveType).init(load); 
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new; 
		
		if(load > 0, {
		
		if(durations.isNil, {
		durations = [0.3, 0.3, 0.2, 0.2];
		if(this.curveType == 1, {durations = [0.3, 0.3, 0.2, 0.2]});
		if(this.curveType == 2, {durations = [0.4, 0.4, 0.1, 0.1]});
		if(this.curveType == 3, {durations = [0.25, 0.0125, 0.0125, 0.25]});
		if(this.curveType == 4, {durations = [0.4, 0.2, 0.3, 0.1]})});
		
		if(amplitudes.isNil, {
		amplitudes = [0.15, 1.0, 0.25];
		if(this.curveType == 1, {amplitudes = [0.6, 0.3, 0.9]});
		if(this.curveType == 2, {amplitudes = [1, 2, 0.25]});
		if(this.curveType == 3, {amplitudes = [0.1, 1.2, 0.1]});
		if(this.curveType == 4, {amplitudes = [1, 0.1, 1]})});
		
		if(amplitudesDuration.isNil, {
		amplitudesDuration = [0.5, 0.5, 0.5];
		if(this.curveType == 1, {amplitudesDuration = [0.6, 0.3, 0.9]});
		if(this.curveType == 2, {amplitudesDuration = [1, 2, 0.25]});
		if(this.curveType == 3, {amplitudesDuration = [0.1, 1.2, 0.1]});
		if(this.curveType == 4, {amplitudesDuration = [1, 0.1, 1]})});
			
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\marbleReso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play; });
	}
	
	setParam {|paramName, paramValue| 
		if(paramName == \duration, {duration = paramValue});
		if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}
	
	*loadSynthDefs {
		
		SynthDef(\marble, { arg bus, freq=1, pan=0.1, amp=1, globalAmp=1, dur=0.5, atk=0.8, sus=1, rel=1, surface=0.0;
		var osca, oscb, oscc, signal, distort, output; 
    		osca = Dust.ar(200) * EnvGen.kr(Env.new([0,1,1,0],[atk, sus, rel]));
		oscb = PinkNoise.ar(0.8) * EnvGen.kr(Env.new([0,1,1,0],[atk, sus, rel])); 
		oscc = Mix.new(Gendy3.ar(freq:([4, 7.2, 5.3, 18.75, 6.3] * LinLin.kr(freq,0.0,1.0, 10,25)  ),mul:0.8)) * EnvGen.kr(Env.new([0,1,1,0],[atk, sus, rel]));
		osca = BLowPass.ar(osca, LinLin.kr(freq,0.0,1.0, 800,3200), 0.9, mul:8);
		oscb = BLowPass.ar(oscb, LinLin.kr(freq,0.0,1.0, 200,1200), 0.9, mul:8);
		oscc = BHiPass.ar(oscc, LinLin.kr(freq,0.0,1.0, 4000,12000), 0.9, mul:6);
		signal = oscb + osca + oscc; 
     	signal = signal * EnvGen.kr(Env.new([1, 0], [dur * 1.5]), doneAction:2);    
		signal = (signal * amp) * globalAmp;
		distort = signal.hypot(Impulse.ar(LinLin.kr(freq,0.0,1.0,10,60)));
		output = (signal * (1-surface)) + (distort * (surface * 0.125)); 
		output = Pan2.ar(output, pan); 
		OffsetOut.ar(0, signal); 
		}).add;
		
		SynthDef(\marbleReso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, roomsize=240, revtime=4.85, damping=0.21, inputbw=0.19, 
		earlylevel=(-12), taillevel=(-11), spread = 15|
		var input, signal; 
		input = In.ar(inBus, 2); 
		
		signal = (GVerb.ar( 
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
		
		Out.ar(outBus, signal);
		}).add;
	}
	
	update {
		
		var pat, dur, grDur, atk, sus, rel, grAtk, grSus, grRel, amp, grAmp, pan;
		var locationFrom, locationTo, locationSpeed, durFrom, durTo, atkFrom, atkTo, susFrom, susTo, relFrom, relTo;
		var rate, surface, entropy;
		
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);

		locationFrom = Env.new([-0.9, -0.6, 0.1, 0.3],[0.33, 0.33, 0.33]).at(control.location);  
		locationTo = Env.new([-0.3, -0.1, 0.6, 0.9],[0.33, 0.33, 0.33]).at(control.location);  
		locationSpeed = Env.new([0.25, 0.75, 1.5, 4.0],[0.33, 0.33, 0.33]).at(control.speed); 

		durFrom = control.speed * control.entropy.linlin(0,1,1.0,0.001);
		durTo = control.speed * control.entropy.linlin(0,1,1.0,2.0);
		atkFrom = Env.new([0.0, 0.01, 0.05, 0.2, 0.8],[0.25, 0.25, 0.25]).at(control.density) * control.entropy.linlin(0,1,1.0,0.001);
		atkTo = Env.new([0.0, 0.08, 0.1, 0.4, 1.5],[0.25, 0.25, 0.25]).at(control.density) * control.entropy.linlin(0,1,1.0,1.5);
		susFrom = Env.new([0.05, 0.08, 0.1, 0.15, 0.2],[0.25, 0.25, 0.25]).at(control.density) * control.entropy.linlin(0,1,1.0,0.001);
		susTo = Env.new([0.08, 0.1, 0.15, 0.2, 0.4],[0.25, 0.25, 0.25]).at(control.density) * control.entropy.linlin(0,1,1.0,2.0);
		relFrom = Env.new([0.05, 0.08, 0.1, 0.15, 0.2],[0.25, 0.25, 0.25]).at(control.density) * control.entropy.linlin(0,1,1.0,0.001);
		relTo = Env.new([0.08, 0.1, 0.15, 0.2, 0.4],[0.25, 0.25, 0.25]).at(control.density) * control.entropy.linlin(0,1,1.0,2.0);
		
		dur = Pwrand([durFrom, durTo, durFrom*1.1, durTo*0.9], this.durations, inf); 
		grAtk = Pwhite(atkFrom, atkTo);
		grSus = Pwhite(susFrom, susTo); 
		grRel = Pwhite(relFrom, relTo);
		rate = control.frequency;
		pan = Pseg( Pseq([locationFrom, locationTo], inf), Pseq([locationSpeed, locationSpeed], inf));
		grAmp = Pseg( Pseq(this.amplitudes * control.amplitude, inf), Pseq(this.amplitudesDuration, inf)); 
		amp = Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')); 
		surface = Env.new([0.0, 1.0],[1.0]).at(control.surface);
		
		this.rev; 
		
	   	if(this.isPlaying > 0,
		{
			if(control.speed != preControl.speed, {'update speed '.postln; proxyDur.source = dur});
			if(control.density != preControl.density || control.entropy != preControl.entropy , {
			'update atack etc '.postln;
			proxyAtk.source = grAtk; proxySus.source = grSus; proxyRel.source = grRel});
			if(control.surface != preControl.surface, {'update spurface '.postln; proxySurface.source = surface});
			if(control.frequency != preControl.frequency, {'update rate '.postln; proxyRate.source = rate});
			if(control.location != preControl.location, {'update pan '.postln; proxyPan.source = pan});
			if(control.amplitude != preControl.amplitude, {'update amp '.postln; proxyGrAmp.source = amp; });  
		}, 
		{
			('Marble start'.postln);
			proxyDur = PatternProxy(dur);  
			proxyAtk = PatternProxy(grAtk);
			proxySus = PatternProxy(grSus);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(rate);
			proxyPan = PatternProxy(pan);
			proxyGrAmp = PatternProxy(grAmp);
			proxyAmp = PatternProxy(amp);
			proxySurface = PatternProxy(surface);
			
			currentPattern.stop;
		
			pat = Pbind(
			\instrument, \marble, 	 
			\dur, proxyDur,
			\atk, proxyAtk,
			\sus, proxySus,
			\rel, proxyRel,  
			\freq, proxyRate,
			\pan, proxyPan, 
	 		\amp, proxyGrAmp,
	 		\globalAmp, proxyAmp,
			\surface, proxySurface,
			\bus, effectBus
		);
		
		this.playDuration(pat);
		this.isPlaying = 1;
		
		});
		
		preControl.copy(this.control); 
	}
	
	rev {
		if(control.color != preControl.color, 
		{
			reverb.set(\dryWet, control.color); 
		});
		if(control.location != preControl.location, 
		{
			reverb.set(\spread, Env.new([2, 4, 8, 15, 50],[0.25, 0.25, 0.25, 0.25]).at(control.location))
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
		buf.free;
		this.isPlaying = 0;
	}
	
	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free}); 
	}
	
	setDescription {
		description = "BSeqMarble";
	}
}
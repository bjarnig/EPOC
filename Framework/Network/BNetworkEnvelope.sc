BNetworkEnvelope
{
	var <>envelope;
	var <>start;
	var <>parameter;
	var <>object;
	var <>granularity;
	
	*new {|envelope, start, parameter, item, granularity|
		
		^super.newCopyArgs(envelope, start, parameter, item, granularity).init();
	}
	
	init {
		
		if(granularity.isNil, {granularity = 0.2}); 
	}
	
	runEnvelope {
	var elapsed = 0.0, val, steps, max=0.999, min=0.009; 
	
	steps = (this.envelope.times.sum) / this.granularity; 
	Routine.new{
	
	steps.do({
	val = this.envelope.at(elapsed);
	this.object.setParamAndUpdate(this.parameter, val.min(max).max(min));
	this.granularity.wait;
	elapsed = elapsed + this.granularity; });
	
	}.play;
	}
	
	runEnvelopeFunction {
	^{this.runEnvelope};
	}
}
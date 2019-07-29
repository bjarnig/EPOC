BNetworkItem
{
	var <>preObject;
	var <>object;
	var <>params;
	var <>name;
	var <>control;
	var <>start;
	var <>duration;
	var <>envelopes;
	var <>initialControl;
	var <>slotPos;

	*new {|preObject, params, name, control, start, duration, envelopes|

		^super.newCopyArgs(preObject, nil, params, name, control, start, duration, envelopes).init();
	}

	init {

		if(control.isNil, {this.control = BControl.new});
	}

	initObject {
	var count = 0, times = 0;
	"init obj 1".postln;
	if(start.isNil, {start = 0});
	if(duration.isNil, {duration = 10});
	if(envelopes.isNil, {envelopes = List.new});
	"init obj 2".postln;
	this.object = this.preObject.new(load:0, duration:this.duration, control:this.control);
	times = (params.size * 0.5);
	"init obj 3".postln;
		times.do({arg name;

			this.object.setParam(params[count], params[count + 1]);
			count = count + 2;
		});
	"init obj 4".postln;
	this.object.postln;
	this.object.init;
	"init obj 5".postln;
	if(this.initialControl.isNil, {this.initialControl = this.control.deepCopy });// pre april 2012 was not deep copy OR NOT IN IF EITHER
	envelopes.do({arg e; e.object = this.object});
	"init obj 6".postln;
	}

	update {|updateValues = 0|

		if(this.object.notNil, {this.object.duration = this.duration; this.object.control = this.control.deepCopy;  // pre april 2012 was not deep copy
		if(updateValues > 0, {
		if(this.object.isPlaying > 0, {this.object.update})})});
	}

	dispose {

		if(this.object.notNil, {this.object.dispose.value; this.object = nil});
	}

	containsEnvelope {|param|
	var output = 0;

		if(this.envelopes.notNil, {

			this.envelopes.do({arg e;
			if(e.parameter == param, {output = 1});
			});
		});

	^output;

	}

	// implemented april 2012
	stop {
		'STOP NETWORK ITEM'.postln;
		if(this.object.notNil, {this.object.stop.value});

	}

	printParams {
	var name=1, output = '[', count=0;

	this.params.do({arg p;
	var val;

		if(p.class == String, {val = '\"' ++ p ++ '\"' }, {val = p});
		if(name == 1, {output = output ++ '\\' ++ val; name = 0}, {output = output ++ val; name = 1});
		if(count < (params.size - 1), {output = output ++ ', '});
		count = count + 1;
	});

	^(output ++ ']');

	}
}
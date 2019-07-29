Bwrap 
{ 
	var <argNames, <argValues, <synthDef, <synth;
	
	*new { |name, values|
		^super.new.init(name, values);
	}
	
	init { |name, values|
		
		synthDef = SynthDescLib.global[name].def;
		argNames = synthDef.allControlNames.reject{ |obj| obj.defaultValue.size > 0}.collect(_.name);
		argValues = IdentityDictionary.new;
		
		argNames.do{ |key,i|
		argValues.put(key, synthDef.controls[i]);
		
		if(values.notNil)
			{ this.setValues(values); }
		}
	}
		
	get{ |key|
		^argValues[key].value;
	}
	
	set{ |key, value|
		argValues[key] = value;
		synth.set(key, value);
	}
	
	setn{ |key, value|
		argValues[key] = value;
		synth.setn(key, value);
	}
	
	setValues{ |values|
		
		var count = values.size - 1;
		count.do({|i| 
		if(values[i].class == Symbol)
			{ this.set(values[i], values[i+1]); } 
		}); 		
	}
	
	play {
	 synth = Synth(synthDef.name, argNames.collect{ |name| [name, argValues[name].value] }.flatten).register;
	}
	
	stop {
				
	 synth.free;
	
	}
}

	
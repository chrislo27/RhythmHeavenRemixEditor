package ionium.util.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Shows a field doesn't get written to NBT if the containing class implements CanBeSavedToNBT
 * 
 *
 */
@Retention(RetentionPolicy.SOURCE)
public @interface NotWrittenToNBT {
	
	
	
}

package org.dcm4che.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.dcm4che3.tool.qc.QCOperation;

@Retention(RetentionPolicy.RUNTIME)
public @interface QCParameters {
	public String url();

	String qcRejectionCodeString();

	public QCOperation operation();

	String targetStudyUID();
}

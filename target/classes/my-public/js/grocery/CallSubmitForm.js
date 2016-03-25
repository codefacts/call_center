site.reactjs.CallSubmitForm = React.createClass({
    render: function () {
        return (
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h3 className="panel-title">{'Question & Answer'}</h3>
                </div>
                <div className="panel-body">
                    <div className="well well-sm">
                        <div className="row ">
                            <div className="col-md-6">Call Agent: <strong>Shova Khan Ahamed</strong></div>
                            <div className="col-md-6">Call Date: <strong>12-Nov-2015</strong></div>
                        </div>
                    </div>
                    <div className="well well-sm">
                        <form className="form-horizontal">

                            <div className="row contact-details-row">
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">Name Match</span>
                                    </label>
                                </div>
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">BR Contact</span>
                                    </label>
                                </div>
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">DOB Matched</span>
                                    </label>
                                </div>
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">Target Brand</span>
                                    </label>
                                </div>
                            </div>

                            <div className="row contact-details-row">
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">Video Shown</span>
                                    </label>
                                </div>
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">Toolkit Shown</span>
                                    </label>
                                </div>
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">PTR</span>
                                    </label>
                                </div>
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">SWP</span>
                                    </label>
                                </div>
                            </div>

                            <div className="row contact-details-row">
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">Refreshment</span>
                                    </label>
                                </div>
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">Give Away</span>
                                    </label>
                                </div>
                                <div className="col-md-3">
                                    <label className="checkbox-inline call_label_inline">
                                        <input className="call_checkbox" type="checkbox" id="inlineCheckbox1"
                                               value="option1"/>
                                        <span className="call_checkbox_label">Pack Sell</span>
                                    </label>
                                </div>

                            </div>


                            <div className="row contact-details-row">
                                <div className="col-md-1">
                                    <span className="call-form-label">Brand</span>
                                </div>
                                <div className="col-md-4">
                                    <select className="form-control call-control" name="call_status">
                                        <option value="">Select Status</option>
                                        <option value="1">Success</option>
                                        <option value="2">Mobile Off</option>
                                        <option value="6">Wrong Number</option>
                                        <option value="8">Call Later on</option>
                                        <option value="9">Not Matched</option>
                                        <option value="10">Others</option>
                                        <option value="11">Not Interested</option>
                                        <option value="13">Duplicate</option>
                                        <option value="14">N/A</option>
                                        <option value="15">N/A</option>
                                        <option value="18">Request No</option>
                                        <option value="19">Call Received by Others</option>
                                        <option value="20">No Answer</option>
                                    </select>
                                </div>
                                <div className="col-md-3">
                                    <span className="call-form-label">Brand Other</span>
                                </div>
                                <div className="col-md-4">
                                    <input type="text" className="form-control" value=""/>
                                </div>

                            </div>

                            <div className="row contact-details-row">
                                <div className="col-md-3">
                                    <span className="call-form-label">Remarks</span>
                                </div>
                                <div className="col-md-9">
                                    <textarea className="form-control"/>
                                </div>

                            </div>

                            <div className="row contact-details-row">
                                <div className="col-md-3">
                                    <strong className="call-form-label">Call Status</strong>
                                </div>
                                <div className="col-md-9">
                                    <select className="form-control call-control" name="call_status">
                                        <option value="">Select Status</option>
                                        <option value="1">Success</option>
                                        <option value="2">Mobile Off</option>
                                        <option value="6">Wrong Number</option>
                                        <option value="8">Call Later on</option>
                                        <option value="9">Not Matched</option>
                                        <option value="10">Others</option>
                                        <option value="11">Not Interested</option>
                                        <option value="13">Duplicate</option>
                                        <option value="14">N/A</option>
                                        <option value="15">N/A</option>
                                        <option value="18">Request No</option>
                                        <option value="19">Call Received by Others</option>
                                        <option value="20">No Answer</option>
                                    </select>
                                </div>
                            </div>

                            <div className="row">
                                <div className="col-md-8"></div>
                                <div className="col-md-2">
                                    <span className="btn btn-danger btn-block">Clear</span>
                                </div>
                                <div className="col-md-2">
                                    <input className="btn btn-primary btn-block" type="submit"
                                           value="Submit"/>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
});
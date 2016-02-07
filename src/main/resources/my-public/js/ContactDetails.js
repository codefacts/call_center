site.reactjs.ContactDetails = React.createClass({
    getDefaultProps: function () {
        return {
            data: {}
        };
    },
    render: function () {
        var $this = this;
        var v = $this.props.data;
        return (
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h3 className="panel-title">Contact Details</h3>
                </div>
                <div className="panel-body">

                    <div className="well well-sm">
                        <div className="row contact-details-row">
                            <div className="col-md-12">
                                Contact ID: <strong>{v.SMS_ID}</strong>
                            </div>
                        </div>
                        <div className="row contact-details-row">
                            <div className="col-md-12">
                                Distribution House: <strong>{v.DISTRIBUTION_HOUSE_NAME}</strong>
                            </div>
                        </div>
                        <div className="row contact-details-row">
                            <div className="col-md-4">
                                BR ID: <strong>{v['s.BR_ID']}</strong>
                            </div>
                            <div className="col-md-8">
                                BR Name: <strong>{v.BR_NAME}</strong>
                            </div>
                        </div>
                        <div className="row contact-details-row">
                            <div className="col-md-12">
                                Contact Date: <strong>{$this.parseDate(v.Date)}</strong>
                            </div>
                        </div>
                        <div className="row contact-details-row">
                            <div className="col-md-7">
                                Remarks: <strong>{v.REMARKS}</strong>
                            </div>
                        </div>
                    </div>

                    <div className="well well-sm">
                        <div className="row contact-details-row">
                            <div className="col-md-6">
                                Consumer Name: <strong>{v.Customer_Name}</strong>
                            </div>
                            <div className="col-md-6">
                                Father Name: <strong>{v['s.FATHER_NAME']}</strong>
                            </div>
                        </div>

                        <div className="row contact-details-row">
                            <div className="col-md-6">
                                Mobile: <strong><input style={{width: '120px', padding: '2px', margin: '0'}}
                                                       readOnly={true} type="text"
                                                       value={v.CONSUMER_MOBILE}/></strong>
                            </div>

                            <div className="col-md-6">
                                Age: <strong>{v['s.AGE']}</strong>
                            </div>
                        </div>
                        <div className="row contact-details-row">
                            <div className="col-md-12">
                                Occupation: <strong>{v.Occupation}</strong>
                            </div>
                        </div>
                        <div className="row contact-details-row">
                            <div className="col-md-6">
                                Email: <strong>{v.Email}</strong>
                            </div>
                            <div className="col-md-6">
                                District: <strong>{v.DISTRICT}</strong>
                            </div>
                        </div>
                        <div className="row contact-details-row">
                            <div className="col-md-12">
                                Address: <strong>{v.ADDRESS}</strong>
                            </div>
                        </div>
                    </div>

                    <div className="well well-sm">
                        <div className="row contact-details-row">
                            <div className="col-md-3">
                                PTR: <span className={$this.yesNo(v['s.PTR'])} aria-hidden="true"></span>
                            </div>
                            <div className="col-md-3">
                                SWP: <span className={$this.yesNo(v['SWAP'])} aria-hidden="true"></span>
                            </div>
                            <div className="col-md-3">
                                Refreshment: <span className={$this.yesNo(v['Refreshmemt'])}
                                                   aria-hidden="true"></span>
                            </div>
                            <div className="col-md-3">
                                Give Away: <span className={$this.yesNo(v['s.GIVE_AWAY'])}
                                                 aria-hidden="true"></span>
                            </div>
                        </div>
                        <div className="row contact-details-row">
                            <div className="col-md-3">
                                Pack Sell: <span className={$this.yesNo(v['PACK_SELL'])} aria-hidden="true"></span>
                            </div>
                            <div className="col-md-3">
                                Tools Shown: <span className={$this.yesNo(v['SHOW_TOOLS'])}
                                                   aria-hidden="true"></span>
                            </div>
                            <div className="col-md-3">
                                Video Shown: <span className={$this.yesNo(v['SHOW_VIDEO'])}
                                                   aria-hidden="true"></span>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        );
    },
    yesNo: function (yesNo) {
        return !!yesNo ? "glyphicon glyphicon-ok" : "glyphicon glyphicon-remove";
    },
    parseDate: function (date) {
        return !date ? "" : formatDate(new Date(parseInt(
            date.substring(date.indexOf('(') + 1, date.lastIndexOf(')')))));
    }
});
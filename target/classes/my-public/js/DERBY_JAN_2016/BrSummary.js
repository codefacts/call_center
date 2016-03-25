site.reactjs.DERBY_JAN_2016.BrSummary = React.createClass({
    getDefaultProps: function () {
        return {data: {}};
    },
    render: function () {
        var v = this.props.data;
        return (
            <div className="panel panel-primary" style={{borderRadius: 0, margin: 0, border: 0}}>

                <div className="panel-body" style={{margin: 0, padding: 0}}>

                    <div className="row">
                        <div className="col-md-12">
                            <table className="table table-stripped DetailsTable alert-success"
                                   style={{marginBottom: '2px'}}>
                                <thead>
                                <tr>
                                    <td></td>
                                    <td>Contact</td>
                                    <td>S. Need</td>
                                    <td>Called</td>
                                    <td>Success</td>
                                    <td className="bg-danger">PTR</td>
                                    <td className="bg-danger">Called</td>
                                    <td className="bg-danger">Success</td>

                                    <td className="bg-info">Refreshment</td>
                                    <td className="bg-info">Called</td>
                                    <td className="bg-info">Success</td>
                                    <td className="bg-danger">G.A.</td>
                                    <td className="bg-danger">Called</td>
                                    <td className="bg-danger">Success</td>
                                    <td className="bg-info">P.S.</td>
                                    <td className="bg-info">Called</td>
                                    <td className="bg-info">Success</td>

                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <th>Total:</th>
                                    <th>{v.total.totalContact}</th>
                                    <th>{v.total.totalSuccessNeed}</th>
                                    <th>{v.total.totalCalled}</th>
                                    <th>{v.total.totalSuccess}</th>
                                    <th className="bg-danger">{v.total.totalPTR}</th>
                                    <th className="bg-danger">{v.total.totalPTRCalled}</th>
                                    <th className="bg-danger">{v.total.totalPTRSuccess}</th>

                                    <th className="bg-info">{v.total.totalRef}</th>
                                    <th className="bg-info">{v.total.totalRefCalled}</th>
                                    <th className="bg-info">{v.total.totalRefSuccess}</th>
                                    <th className="bg-danger">{v.total.totalGA}</th>
                                    <th className="bg-danger">{v.total.totalGACalled}</th>
                                    <th className="bg-danger">{v.total.totalGASuccess}</th>
                                    <th className="bg-info">{v.total.totalPackSell}</th>
                                    <th className="bg-info">{v.total.totalPackSellCalled}</th>
                                    <th className="bg-info">{v.total.totalPackSellSuccess}</th>

                                </tr>
                                <tr>
                                    <th>Daily:</th>
                                    <th>{v.daily.totalContact}</th>
                                    <th>{v.daily.totalSuccessNeed}</th>
                                    <th>{v.daily.totalCalled}</th>
                                    <th>{v.daily.totalSuccess}</th>
                                    <th className="bg-danger">{v.daily.totalPTR}</th>
                                    <th className="bg-danger">{v.daily.totalPTRCalled}</th>
                                    <th className="bg-danger">{v.daily.totalPTRSuccess}</th>

                                    <th className="bg-info">{v.daily.totalRef}</th>
                                    <th className="bg-info">{v.daily.totalRefCalled}</th>
                                    <th className="bg-info">{v.daily.totalRefSuccess}</th>
                                    <th className="bg-danger">{v.daily.totalGA}</th>
                                    <th className="bg-danger">{v.daily.totalGACalled}</th>
                                    <th className="bg-danger">{v.daily.totalGASuccess}</th>
                                    <th className="bg-info">{v.daily.totalPackSell}</th>
                                    <th className="bg-info">{v.daily.totalPackSellCalled}</th>
                                    <th className="bg-info">{v.daily.totalPackSellSuccess}</th>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
});
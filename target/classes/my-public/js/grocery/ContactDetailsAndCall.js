site.reactjs.ContactDetailsAndCall = React.createClass({
    render: function () {
        return (
            <div className="row">
                <div className="col-md-6">
                    <site.reactjs.ContactDetails />
                </div>
                <div className="col-md-6">
                    <site.reactjs.CallSubmitForm />
                </div>
            </div>
        );
    }
});
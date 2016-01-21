site.reactjs.Step1 = React.createClass({
    getDefaultProps: function () {
        return {
            onInit: function () {
            }
        }
    },
    getInitialState: function () {
        var $this = this;
        var filterFieldsScope = function () {
            var scope = null;
            return {
                set: function (state) {
                    scope = state;
                },
                get: function () {
                    return scope;
                }
            };
        };
        console.log("INIT>>>")
        return {
            filterFieldsRef: null,
            primaryTableRef: null,
            filterFields: <site.reactjs.FilterFields formId="filter-form" scope={filterFieldsScope()}
                                                     onFilterFiledsInit={$this.onFilterFiledsInit}/>,
            ViewFilter: 1,
            pagination: {}
        };
    },
    componentDidMount: function () {
        var $this = this;
        $this.updateData(site.hash.params());
        $this.props.onInit($this);
    },
    render: function () {
        var $this = this;
        var pagination = $this.state.pagination;
        return (
            <div className="row">

                <div id="container" className="col-md-12">

                    <site.reactjs.TablePrimary onInit={$this.onPrimaryTableInit}/>

                    <Pagination page={pagination.page} size={pagination.size} total={pagination.total}
                                onPageRequest={$this.onPageRequest} navLength={20}/>
                </div>

                {(function () {
                    if ($this.isViewFilterArrow()) {
                        return (<site.reactjs.FilterArrow onClick={$this.onFilterClick}/>);
                    } else if ($this.isViewFilter()) {
                        return (<site.reactjs.Filter onHeaderClick={$this.onFilterClick}
                                                     onSubmitButtonClick={$this.updateQueryString}
                                                     onClearButtonClick={$this.clearQueryString}
                                                     body={$this.state.filterFields}/>);
                    }
                })()}

            </div>
        );
    },
    onFilterFiledsInit: function (filterFieldsRef) {
        this.setState({filterFieldsRef: filterFieldsRef});
    },
    onPrimaryTableInit: function (primaryTableRef) {
        this.setState({primaryTableRef: primaryTableRef});
    },
    updateQueryString: function () {
        site.hash.setParams($('#filter-form').serializeArray());
    },
    updateData: function (params) {
        var $this = this;
        if (!!$this.state.filterFieldsRef) $this.state.filterFieldsRef.updateFields(params);
        $.ajax({
            url: '/consumer-contacts/call-step-1',
            data: params,
            success: function (js) {
                var data = [];
                js.data = js.data || [];
                js.data.forEach(function (obj) {
                    data.push(obj)
                    var oo = {type: 'details'};
                    for (var x in obj.others) {
                        oo[x] = obj.others[x];
                    }
                    oo.br_id = obj.br_id;
                    oo.date = obj.date;
                    data.push(oo);
                });

                console.log({page: js.pagination.page, size: js.pagination.size, total: js.pagination.total});

                $this.setState({
                    pagination: {page: js.pagination.page, size: js.pagination.size, total: js.pagination.total}
                });
                $this.state.primaryTableRef.updateData(data);
            },
        });
    },
    onPageRequest: function (page, size) {
        site.hash.goto('/', merge2(site.hash.params(), {page: page, size: size}));
    },
    clearQueryString: function () {
        var prms = $('#filter-form').serializeArray().map(function (e) {
            return e.name;
        });
        site.hash.removeAll(prms);
    },
    isViewFilter: function () {
        var $this = this;
        return $this.state.ViewFilter == 2;
    },
    isViewFilterArrow: function () {
        var $this = this;
        return $this.state.ViewFilter == 1;
    },
    onFilterClick: function () {
        var $this = this;
        $this.setState({ViewFilter: $this.state.ViewFilter == 2 ? 1 : 2});
    },
    router: function () {
        var $this = this;
        //site.hash
        //    .on('/work-day-details', function () {
        //        $this.setState({
        //            view: <site.reactjs.WorkDayDetailsTable />,
        //            filterFields: <site.reactjs.FilterFields2 formId="filter-form"/>,
        //            ViewFilter: 1
        //        });
        //    })
        //    .on('/call', function () {
        //        $this.setState({
        //            view: <site.reactjs.ContactDetailsAndCall />,
        //            ViewFilter: 0
        //        });
        //    })
        //;
    }
});
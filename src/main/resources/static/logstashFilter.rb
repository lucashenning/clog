# Call this file 'logstashFilter.rb' (in logstash/filters, as above)
require "logstash/filters/base"
require "logstash/namespace"

class LogStash::Filters::CLog < LogStash::Filters::Base

  # Setting the config_name here is required. This is how you
  # configure this filter from your logstash config.
  #
  # filter {
  #   clog { ... }
  # }
  config_name "clog"

  # New plugins should start life at milestone 1.
  milestone 1

  # Replace the message with this value.
  config :url, :validate => :string, :required => true
  config :timestamp, :validate => :string, :required => true
  config :user, :validate => :string, :required => true
  config :password, :validate => :string, :required => true
  config :timeout, :validate => :string, :required => true

  public
  def register
    require "json"
    require "rest_client"
    @resource = RestClient::Resource.new(@url,
        :user     => @username,
        :password => @password,
        :timeout  => @timeout)
  end # def register

  public
  def filter(event)
    return unless filter?(event)
    response = @resource.get(:accept => 'json', :params => {:timestamp => timestamp})
    responseHash = JSON.parse(response)
    key = responseHash["key"]
    event['key'] = key
    filter_matched(event)
  end
end # class LogStash::Filters::CLog
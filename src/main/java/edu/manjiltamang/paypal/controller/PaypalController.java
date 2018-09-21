package edu.manjiltamang.paypal.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import edu.manjiltamang.paypal.service.URLLocation;


@Controller
@RequestMapping("/paypal")
public class PaypalController {	
	@Value("${paypal.mode}")
	private String mode;
	@Value("${paypal.client.app}")
	private String clientID;
	@Value("${paypal.client.secret}")
	private String clientSecret;
	
	@Autowired
	private APIContext apiContext;
	
	@GetMapping()
	public String paypalPay(HttpServletRequest req) {

		// ### Items
		Item itemOne = new Item();
		itemOne.setName("Ground Coffee 40 oz").setQuantity("1").setCurrency("USD").setPrice("5");
		Item itemTwo = new Item();
		itemTwo.setName("Cat collor").setQuantity("2").setCurrency("USD").setPrice("15");


		// Adding item to our list
		List<Item> items = new ArrayList<Item>();
		items.add(itemOne);
		items.add(itemTwo);

		// Adding items to itemList
		ItemList itemList = new ItemList();
		itemList.setItems(items);

		// Set payment details
		Details details = new Details();
		details.setShipping("1");
		// itemOne cost: 5 itemTwo cost 15*2. So 5+30
		details.setSubtotal("35");
		details.setTax("1");

		// Payment amount
		Amount amount = new Amount();
		amount.setCurrency("USD");
		// Total must be equal to sum of shipping, tax and subtotal. E.g. 1+35+1
		amount.setTotal("37");
		amount.setDetails(details);

		// Transaction information
		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setDescription("This is the payment transaction description.");

		transaction.setItemList(itemList);

		// The Payment creation API requires a list of
		// Transaction; add the created `Transaction`
		// to a List
		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);

		// ###Payer
		// A resource representing a Payer that funds a payment
		// Payment Method
		// as 'paypal'
		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");

		// Add payment details
		Payment payment = new Payment();
		payment.setIntent("sale");
		payment.setPayer(payer);
		// payment.setRedirectUrls(redirectUrls);
		payment.setTransactions(transactions);

		// ###Redirect URLs
		RedirectUrls redirectUrls = new RedirectUrls();
		String guid = UUID.randomUUID().toString().replaceAll("-", ""); // Not necessary, just demonstrating how we can add the order/user id as a param.
		// Payment cancellation url
		redirectUrls.setCancelUrl(URLLocation.getBaseUrl(req) + "/paypal/payment/cancel?guid=" + guid);
		// Payment success url
		redirectUrls.setReturnUrl(URLLocation.getBaseUrl(req) + "/paypal/payment/success?guid=" + guid);
		payment.setRedirectUrls(redirectUrls);

		// Create payment
		try {
			Payment createdPayment = payment.create(apiContext);

			// ###Payment Approval Url
			Iterator<Links> links = createdPayment.getLinks().iterator();
			while (links.hasNext()) {
				Links link = links.next();
				if (link.getRel().equalsIgnoreCase("approval_url")) {
					// redirecting to paypal site for handling payment
					return "redirect:" + link.getHref();
				}
			}

		} catch (PayPalRESTException e) {
			System.err.println(e.getDetails());
			return "redirect:/paypal/error";
		}
		return "redirect:/paypal/error";

	}
	
	@GetMapping("/payment/success")
	@ResponseBody
	public String executePayment(HttpServletRequest req) {
		Payment payment = new Payment();
		payment.setId(req.getParameter("paymentId"));

		PaymentExecution paymentExecution = new PaymentExecution();
		paymentExecution.setPayerId(req.getParameter("PayerID"));
		try {
		  Payment createdPayment = payment.execute(apiContext, paymentExecution);
		  System.out.println(createdPayment);
		  return "Success";
		} catch (PayPalRESTException e) {
		  System.err.println(e.getDetails());
		  return "Failed";
		}
	} 
	
	@GetMapping("/payment/cancel")
	@ResponseBody
	public String cancelPayment() {
		return "Payment cancelled";
	}	
}



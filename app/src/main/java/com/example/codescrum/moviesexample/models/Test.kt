package com.grability.core.newworld.controllers

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import com.grability.base.utils.ViewUtil
import com.grability.core.newworld.analytics.AnalyticsStrings
import com.grability.core.newworld.di.container.ReduxContainer.Companion.analyticsActions
import com.grability.core.newworld.di.container.ReduxContainer.Companion.navigatorActions
import com.grability.core.newworld.interfaces.DialogFragmentView
import com.grability.core.newworld.interfaces.FragmentView
import com.grability.core.newworld.managers.account.NewWorldAccountManager
import com.grability.core.newworld.redux.SectionState
import com.grability.core.newworld.redux.SectionType
import com.grability.core.newworld.redux.appStore
import com.grability.core.newworld.ui.activities.*
import com.grability.core.newworld.ui.activities.checkout.CheckoutActivity
import com.grability.core.newworld.ui.activities.deliverytype.DeliveryTypeFlowActivity
import com.grability.core.newworld.ui.fragments.checkout.WhimsFragment
import com.grability.core.newworld.ui.fragments.dialogFragment.CommentFragment
import com.grability.core.newworld.ui.fragments.myAddresses.MyAddressesFragment
import com.grability.core.newworld.ui.fragments.myList.MyListFragment
import com.grability.core.newworld.ui.fragments.orders.MyOrdersFragment
import com.grability.core.newworld.ui.fragments.productdetail.AddToListFragment
import com.grability.core.newworld.utils.navigation.NavigationRoute

/**
 * Manage whole the navigation in the app,
 * its purpose is launch an activity or replace a fragment
 */
open class NavigationController( private var originActivity : BaseFragmentActivity ) : BaseController() {

    private var pendingRoute : NavigationRoute? = null
    private var pendingFragment : FragmentView? = null

    private val loginRequiredRoutes : List<String> = listOf(
            WhimsFragment::class.java.name,
            MyOrdersFragment::class.java.name,
            MyAddressesFragment::class.java.name,
            CheckoutActivity::class.java.name,
            PaymentActivity::class.java.name,
            MyListFragment::class.java.name,
            ProfileActivity::class.java.name,
            CommentFragment::class.java.name,
            AddToListFragment::class.java.name
    )

    companion object {
        const val REQUIRED_LOGIN_REQUEST_CODE : Int = 5730
        const val REQUIRED_LOGIN_SUCCESS_RESULT_CODE: Int = 5530
    }

    override fun inject() {
        component.inject(this)
    }

    fun goTo(fragmentView : FragmentView, addToStack: Boolean) = goToRoute(NavigationRoute.FragmentRoute(fragmentView),addToStack)

    fun goTo(fragmentView : FragmentView) = goTo(fragmentView, true)

    fun goTo(activityRoute : Class<out BaseFragmentActivity>,
             bundle : Bundle? = null) = goToRoute(NavigationRoute.ActivityRoute(activityRoute).setBundle(bundle),true)

    fun goTo(activityRoute : Class<out BaseFragmentActivity>,
             bundle : Bundle? = null, flag: Int? = null) = goToRoute(NavigationRoute.ActivityRoute(activityRoute).setBundle(bundle).setFlag(flag),true)

    fun goToWaitingResult(activityRoute : Class<out BaseFragmentActivity>, requestCode : Int,
                          bundle : Bundle? = null, flag: Int? = null, addToStack: Boolean) {
        val route = NavigationRoute.ActivityRoute(activityRoute)
                                    .setRequestCode(requestCode)
                                    .setBundle(bundle)
                                    .setFlag(flag)
        goToRoute(route,addToStack)
    }

    fun goToWaitingResult(activityRoute : Class<out BaseFragmentActivity>, requestCode : Int,
                          bundle : Bundle? = null, flag: Int? = null) {
        goToWaitingResult(activityRoute,requestCode,bundle,flag, true)
    }

    fun goToAndAddToBackStack(fragmentView : FragmentView) {
        fragmentView.addOnStack()
        goTo(fragmentView)
    }

    fun animatedGoTo(activityRoute: Class<out BaseFragmentActivity>, bundle: Bundle, requestCode: Int, flag: Int?, transitionAnimation: ActivityOptionsCompat?){
        var animationBundle: Bundle? = null
        if (ViewUtil.isMinLollipop()) {
            transitionAnimation?.let { animationBundle = it.toBundle() }
        }
        val route = NavigationRoute.ActivityRoute(activityRoute)
                .setRequestCode(requestCode)
                .setBundle(bundle)
                .setAnimationBundle(animationBundle)
                .setFlag(flag)
        goToRoute(route,true)
    }

    fun present(dialogFragmentView: DialogFragmentView){
        val navigationRoute = NavigationRoute.DialogFragmentRoute(dialogFragmentView)
        if (shouldGoToLogin(navigationRoute.getRouteName())){
            goToLogin(navigationRoute)
        } else if(!dialogFragmentView.isAdded){
            dialogFragmentView.show(getFragmentManager(),dialogFragmentView.name)
        }
    }

    fun onLoginSuccess() {
        pendingRoute?.let {
            when (it){
                is NavigationRoute.DialogFragmentRoute -> present(it.dialogFragmentView)
                else -> goToRoute(it,true)
            }
            pendingRoute = null
        }
    }

    open fun onBack(){
        if(getFragmentManager().backStackEntryCount != 0) {
            exitSection()
            getFragmentManager().popBackStack()
        } else {
            closeActivity()
        }
    }

    fun closeActivity() {
        exitSection()
        getOriginContext().finishAfterTransition()
    }

    protected fun goToRoute(navigationRoute : NavigationRoute, addToStack: Boolean) {
        when {
            shouldGoToLogin(navigationRoute.getRouteName()) -> goToLogin(navigationRoute)
            navigationRoute is NavigationRoute.ActivityRoute -> navigateToActivity(navigationRoute, addToStack)
            navigationRoute is NavigationRoute.FragmentRoute -> navigateToFragment(navigationRoute, addToStack)
        }
    }

    private fun goToLogin(navigationRoute : NavigationRoute) {
        pendingRoute = navigationRoute
        val intent = Intent(getOriginContext(), AccountActivity().javaClass)
        intent.putExtra(AccountActivity.ADVISE_TEXT, navigationRoute.getLoginAdviceText())
        getOriginContext().startActivityForResult(intent, REQUIRED_LOGIN_REQUEST_CODE)
    }

    open fun navigateToFragment(fragmentRoute : NavigationRoute.FragmentRoute, addToStack: Boolean){
        val fragmentView = fragmentRoute.fragmentView
        if((getOriginContext()).isAppInBackground) {
            pendingFragment = fragmentView
        }
        val fragmentTransaction = getFragmentManager().beginTransaction()
        if (fragmentView.transactionStrategy == FragmentView.TransactionStrategy.REPLACE) {
            fragmentTransaction.replace(fragmentView.container, fragmentView, fragmentView.name)
        } else {
            fragmentTransaction.add(fragmentView.container, fragmentView, fragmentView.name)
        }
        if (fragmentView.isAnimate) {
            fragmentTransaction.setCustomAnimations(fragmentView.enter, fragmentView.exit, fragmentView.popEnter, fragmentView.popExit)
        }
        if (fragmentView.isAddOnStack && !(getOriginContext()).isAppInBackground) {
            fragmentTransaction.addToBackStack(fragmentView.name)
        }
        fragmentTransaction.commit()
        if(addToStack) {
            addSection(fragmentRoute)
        }
        registerLoadScreen(fragmentRoute)
    }

    fun onAppToForeground() {
        if (pendingFragment != null) {
            goTo(pendingFragment!!)
            pendingFragment = null
        }
    }

    open fun navigateToActivity(activityRoute: NavigationRoute.ActivityRoute, addToStack: Boolean){
        val intent = Intent(getOriginContext(), activityRoute.getDestinationClass())
        activityRoute.bundle?.let { intent.putExtras(it) }
        activityRoute.flag?.let { intent.addFlags(it) }
        if(activityRoute.animationBundle == null) {
            if (activityRoute.requestCode != null) {
                getOriginContext().startActivityForResult(intent, activityRoute.requestCode!!)
            } else {
                getOriginContext().startActivity(intent)
            }
        }else{
            if (activityRoute.requestCode != null) {
                getOriginContext().startActivityForResult(intent, activityRoute.requestCode!!, activityRoute.animationBundle)
            } else {
                getOriginContext().startActivity(intent, activityRoute.animationBundle)
            }
        }
        if(addToStack) {
            addSection(activityRoute)
        }
        registerLoadScreen(activityRoute)
    }

    private fun isLoginRequired(route : String) = loginRequiredRoutes.contains(route)

    private fun shouldGoToLogin(route : String) :Boolean = isLoginRequired(route) && !isUserLogged()

    fun getOriginContext() : BaseFragmentActivity = originActivity

    private fun getFragmentManager() =originActivity.supportFragmentManager

    fun getLastFragmentFromBackStack() : FragmentView? {
        val index = getFragmentManager().backStackEntryCount - 1
        return if(index >= 0) {
            val backEntry = getFragmentManager().getBackStackEntryAt(index)
            val tag = backEntry.name
            getFragmentManager().findFragmentByTag(tag) as FragmentView
        } else null
    }

    private fun addSection(navigationRoute: NavigationRoute) {
        if (navigationRoute.shouldAddSection()){
            appStore.dispatch(navigatorActions.addSection(navigationRoute.getSectionState()))
        }
    }

    protected fun registerLoadScreen(navigationRoute: NavigationRoute) {
        if(navigationRoute.hasLoadScreen()) {
            appStore.dispatch(analyticsActions.registerEventWithParams(AnalyticsStrings.EVENT_LOAD_SCREEN, arrayOf(AnalyticsStrings.PARAM_SCREEN_NAME), arrayOf(navigationRoute.getScreenName())))
        }
    }

    private fun exitSection() {
        if (appStore.getState().navigation.sectionStack.size > 1){
            val sectionToRemove = appStore.getState().navigation.sectionStack.peek()
            if (!belongsToDeliveryTypeFlowActivity(sectionToRemove)) {
                appStore.dispatch(navigatorActions.exitSection(sectionToRemove))
            }
        }
    }

    private fun NavigationRoute.shouldAddSection() : Boolean {
        return (this.getDestinationClass() == MainActivity::class.java
                || !belongsToDeliveryTypeFlowActivity(this.getSectionState()))
                && this.getDestinationClass() != CheckoutActivity::class.java
    }

    private fun belongsToDeliveryTypeFlowActivity(section: SectionState): Boolean {
        return section.sectionType == SectionType.DELIVERY_TYPE_ACTIVITY
                || getOriginContext() is DeliveryTypeFlowActivity
    }

    private fun isUserLogged(): Boolean {
        return NewWorldAccountManager(getOriginContext()).isUserLogged
    }

}

